package com.sanjid.browser.torrent;

import android.app.*;
import android.os.*;
import android.content.*;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public final class MainActivity extends Activity {
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final ExecutorService io=Executors.newSingleThreadExecutor();
    private TextView status,stats; private ProgressBar progress; private EditText input;
    private File exportFile;
    private final Runnable update=new Runnable(){public void run(){render();handler.postDelayed(this,700);}};
    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(0xff10152b);getWindow().setNavigationBarColor(0xff10152b);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);LinearLayout page=new LinearLayout(this);page.setOrientation(1);page.setPadding(dp(24),dp(28),dp(24),dp(24));
        page.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xff10152b,0xff282044,0xff102b3b}));scroll.addView(page);setContentView(scroll);
        text(page,"Z  /  TORRENT LAB",14,0xff9bc7ff);text(page,"Transfers",34,Color.WHITE);text(page,"ENGINE TEST · 0.1.0",12,0xffc7b9f7);
        LinearLayout card=card(page);text(card,"Add a torrent",20,Color.WHITE);text(card,"Peer-to-peer transfers share your IP address with peers and may upload data during download. Use content you are authorized to share.",14,0xffcbd1ea);
        input=new EditText(this);input.setHint("magnet:?xt=…");input.setHintTextColor(0xffa9b6d5);input.setTextColor(Color.WHITE);input.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE);card.addView(input);
        button(card,"Add magnet",()->{String m=input.getText().toString().trim();if(!InputRules.validMagnet(m)){toast("Invalid magnet link");return;}confirm(m,null);});
        button(card,"Import .torrent",()->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("*/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,11);});
        LinearLayout transfer=card(page);status=text(transfer,"Idle",19,Color.WHITE);progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);progress.setMax(100);transfer.addView(progress,new LinearLayout.LayoutParams(-1,dp(14)));stats=text(transfer,"No active transfer",14,0xffd9ddf0);
        button(transfer,"Pause",()->cmd(TorrentService.PAUSE));button(transfer,"Resume",()->cmd(TorrentService.RESUME));button(transfer,"Stop transfer / engine",()->cmd(TorrentService.STOP));
        button(transfer,"Export completed file",this::chooseExport);text(page,"Local engine diagnostics are available in Logcat using tag ZTorrent. This lab is not the Brave browser.",12,0xffcbd1ea);
        if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)!=android.content.pm.PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS},12);
    }
    private int dp(int x){return Math.round(x*getResources().getDisplayMetrics().density);}
    private TextView text(LinearLayout p,String value,int size,int color){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);t.setPadding(0,dp(5),0,dp(5));p.addView(t);return t;}
    private LinearLayout card(LinearLayout p){LinearLayout v=new LinearLayout(this);v.setOrientation(1);v.setPadding(dp(16),dp(16),dp(16),dp(16));GradientDrawable d=new GradientDrawable();d.setColor(0x354f5680);d.setCornerRadius(dp(24));d.setStroke(dp(1),0x55ffffff);v.setBackground(d);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.topMargin=dp(18);p.addView(v,lp);return v;}
    private void button(LinearLayout p,String label,Runnable r){Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setOnClickListener(v->r.run());p.addView(b,new LinearLayout.LayoutParams(-1,-2));}
    private void confirm(String m,String path){new AlertDialog.Builder(this).setTitle("Start torrent?").setMessage("This uses peer-to-peer networking and can upload pieces. Only download and share authorized content.").setNegativeButton("Cancel",null).setPositiveButton("Start",(d,w)->{Intent i=new Intent(this,TorrentService.class).setAction(TorrentService.ADD).putExtra("magnet",m).putExtra("meta",path);startForegroundService(i);}).show();}
    private void cmd(String action){if(TorrentService.snapshot==null){toast("No active transfer");return;}startService(new Intent(this,TorrentService.class).setAction(action));}
    private void render(){Transfer t=TorrentService.snapshot;status.setText(TorrentService.message);if(t==null){progress.setIndeterminate(false);progress.setProgress(0);stats.setText("No active transfer");return;}int pct=t.percent();progress.setIndeterminate(pct<0&&t.state==Transfer.State.METADATA);progress.setProgress(Math.max(0,pct));stats.setText(t.title+"\n"+(pct<0?"Size unknown":pct+"%")+" · "+bytes(t.done)+" / "+(t.total<0?"—":bytes(t.total))+"\n↓ "+bytes(t.downRate)+"/s  ↑ "+bytes(t.upRate)+"/s\nPeers "+t.peers+" · Seeds "+t.seeds+" · ETA "+(t.etaSeconds()<0?"—":t.etaSeconds()+"s"));}
    private String bytes(long n){return android.text.format.Formatter.formatShortFileSize(this,n);}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
    private void chooseExport(){File dir=TorrentService.completedDirectory;if(dir==null||TorrentService.snapshot!=null){toast("Complete the transfer, then stop the engine first.");return;}ArrayList<File> files=new ArrayList<>();try{collect(dir,dir,files,0);}catch(IOException e){toast("Cannot read files");return;}if(files.isEmpty()){toast("No completed files");return;}String[] labels=files.stream().map(f->dir.toURI().relativize(f.toURI()).getPath()).toArray(String[]::new);new AlertDialog.Builder(this).setTitle("Export a file").setItems(labels,(d,w)->{exportFile=files.get(w);Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT).setType("application/octet-stream").addCategory(Intent.CATEGORY_OPENABLE).putExtra(Intent.EXTRA_TITLE,exportFile.getName());startActivityForResult(i,13);}).show();}
    private static void collect(File root,File dir,ArrayList<File> files,int depth)throws IOException{if(depth>20||files.size()>=1000)return;File[] children=dir.listFiles();if(children==null)return;for(File f:children){if(files.size()>=1000)break;if(!InputRules.inside(root,f)||!f.getAbsoluteFile().equals(f.getCanonicalFile()))continue;if(f.isDirectory())collect(root,f,files,depth+1);else if(f.isFile())files.add(f);}}
    @Override protected void onActivityResult(int req,int result,Intent data){super.onActivityResult(req,result,data);if(result!=RESULT_OK||data==null||data.getData()==null)return;Uri uri=data.getData();if(req==11){io.execute(()->{try(InputStream in=getContentResolver().openInputStream(uri)){if(in==null)throw new IOException();byte[] b=InputRules.readLimited(in,4*1024*1024);File dir=new File(getCacheDir(),"torrent-inbox");dir.mkdirs();File f=File.createTempFile("metadata-",".torrent",dir);try(FileOutputStream out=new FileOutputStream(f)){out.write(b);}runOnUiThread(()->confirm(null,f.getAbsolutePath()));}catch(Exception e){runOnUiThread(()->toast("Could not import torrent"));}});}else if(req==13&&exportFile!=null){File file=exportFile;io.execute(()->{try(InputStream in=new FileInputStream(file);OutputStream out=getContentResolver().openOutputStream(uri,"w")){if(out==null)throw new IOException();byte[] b=new byte[65536];int n;while((n=in.read(b))!=-1)out.write(b,0,n);runOnUiThread(()->toast("File exported"));}catch(Exception e){runOnUiThread(()->toast("Export failed; destination may contain partial data"));}});}}
    @Override protected void onStart(){super.onStart();handler.post(update);}
    @Override protected void onStop(){handler.removeCallbacks(update);super.onStop();}
    @Override protected void onDestroy(){io.shutdown();super.onDestroy();}
}
