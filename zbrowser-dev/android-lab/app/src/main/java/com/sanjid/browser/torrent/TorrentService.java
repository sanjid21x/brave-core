package com.sanjid.browser.torrent;

import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.os.*;
import android.util.Log;
import com.frostwire.jlibtorrent.*;
import com.frostwire.jlibtorrent.swig.torrent_flags_t;
import java.io.*;
import java.util.concurrent.*;

/** A deliberately single-job engine spike. All native calls are serialized off the UI thread. */
public final class TorrentService extends Service {
    static final String ADD="add", PAUSE="pause", RESUME="resume", STOP="stop";
    static volatile Transfer snapshot;
    static volatile String message="Idle — no transfer";
    static volatile File completedDirectory;
    private final ScheduledExecutorService worker=Executors.newSingleThreadScheduledExecutor();
    private SessionManager session;
    private boolean paused, finished;
    private long started;
    private File target;
    private String jobId;
    private static final String CHANNEL="z_torrent";

    @Override public void onCreate(){
        super.onCreate();
        getSystemService(NotificationManager.class).createNotificationChannel(new NotificationChannel(CHANNEL,"Torrent transfers",NotificationManager.IMPORTANCE_LOW));
        worker.scheduleWithFixedDelay(this::tick,1,1,TimeUnit.SECONDS);
    }
    @Override public int onStartCommand(Intent i,int flags,int startId){
        if(i==null){stopSelf();return START_NOT_STICKY;}
        String action=i.getAction();
        if(ADD.equals(action)){
            String magnet=i.getStringExtra("magnet"), meta=i.getStringExtra("meta");
            startForeground(71,notification("Preparing torrent"),ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
            worker.execute(()->add(magnet,meta));
        }else if(PAUSE.equals(action)) worker.execute(()->control(true));
        else if(RESUME.equals(action)) worker.execute(()->control(false));
        else if(STOP.equals(action)) worker.execute(()->{shutdownEngine(); snapshot=null; message="Stopped. Partial data retained.";stopSelf();});
        return START_NOT_STICKY;
    }
    private void add(String magnet,String metadata){
        if(session!=null){message="Stop current job before adding another.";return;}
        try{
            completedDirectory=null; paused=false; finished=false;
            jobId=java.util.UUID.randomUUID().toString();
            File root=new File(getFilesDir(),"torrent-data");
            target=new File(root,jobId);
            if(!target.mkdirs())throw new IOException("Cannot create storage directory");
            if(target.getUsableSpace()<32L*1024*1024)throw new IOException("Low storage");
            session=new SessionManager(); session.start();
            session.maxConnections(50); session.uploadRateLimit(64*1024);
            if(magnet!=null){
                if(!InputRules.validMagnet(magnet))throw new IllegalArgumentException("Invalid magnet");
                session.download(magnet,target,new torrent_flags_t());
            }else{
                File inbox=new File(getCacheDir(),"torrent-inbox"); File source=new File(metadata==null?"":metadata);
                if(!InputRules.inside(inbox,source)||!source.isFile()||source.length()>4*1024*1024)throw new IOException("Invalid metadata file");
                TorrentInfo ti=new TorrentInfo(source);
                if(!ti.isValid())throw new IOException("Invalid torrent metadata");
                session.download(ti,target);
            }
            started=SystemClock.elapsedRealtime();
            snapshot=new Transfer(jobId,"Waiting for metadata",Transfer.Kind.TORRENT,Transfer.State.METADATA,0,-1,0,0,0,0);
            message="Connecting to peers…";
        }catch(Exception | LinkageError e){fail(e);}
    }
    private void tick(){
        if(session==null||finished)return;
        try{
            TorrentHandle[] handles=session.getTorrentHandles();
            if(handles.length==0){if(SystemClock.elapsedRealtime()-started>30000)throw new IOException("Torrent was not added within 30 seconds");return;}
            TorrentHandle h=handles[0]; if(!h.isValid())return;
            TorrentStatus s=h.status();
            if(s.errorCode().value()!=0)throw new IOException("Torrent engine error code "+s.errorCode().value());
            boolean complete=s.isFinished(), known=s.hasMetadata();
            // Session pause avoids AUTO_MANAGED resuming an individually paused torrent.
            Transfer.State state=complete?Transfer.State.COMPLETED:paused?Transfer.State.PAUSED:!known?Transfer.State.METADATA:Transfer.State.DOWNLOADING;
            long total=known?s.totalWanted():-1;
            snapshot=new Transfer(jobId,s.name(),Transfer.Kind.TORRENT,state,s.totalWantedDone(),total,s.downloadRate(),s.uploadRate(),s.numPeers(),s.numSeeds());
            message=state.toString();
            if(complete){session.pause();finished=true;completedDirectory=target;message="Complete — seeding paused. Export files after stopping.";}
            getSystemService(NotificationManager.class).notify(71,notification(message));
        }catch(Exception | LinkageError e){fail(e);}
    }
    private void control(boolean p){if(session==null||finished)return;try{if(p)session.pause();else session.resume();paused=p;tick();}catch(Exception|LinkageError e){fail(e);}}
    private Notification notification(String text){
        Intent open=new Intent(this,MainActivity.class);
        PendingIntent pi=PendingIntent.getActivity(this,0,open,PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
        return new Notification.Builder(this,CHANNEL).setSmallIcon(android.R.drawable.stat_sys_download).setContentTitle("Z Browser · Torrent Lab").setContentText(text).setContentIntent(pi).setOngoing(true).build();
    }
    private void fail(Throwable e){
        // No magnet URLs, tracker tokens, or local file names in logcat.
        Log.w("ZTorrent","Engine error: "+e.getClass().getSimpleName());
        message="Engine error: "+e.getClass().getSimpleName();
        Transfer old=snapshot;
        snapshot=new Transfer(jobId==null?"error":jobId,"Transfer failed",Transfer.Kind.TORRENT,Transfer.State.FAILED,old==null?0:old.done,old==null?-1:old.total,0,0,0,0);
        shutdownEngine();stopSelf();
    }
    private void shutdownEngine(){if(session!=null){try{session.stop();}catch(Exception|LinkageError ignored){}session=null;}}
    @Override public void onTimeout(int startId,int fgsType){message="Android background transfer limit reached"; Transfer t=snapshot; if(t!=null && t.state!=Transfer.State.COMPLETED) snapshot=new Transfer(t.id,t.title,t.kind,Transfer.State.INTERRUPTED,t.done,t.total,0,0,0,0);stopSelf();}
    @Override public void onDestroy(){worker.execute(this::shutdownEngine);worker.shutdown();super.onDestroy();}
    @Override public IBinder onBind(Intent intent){return null;}
}
