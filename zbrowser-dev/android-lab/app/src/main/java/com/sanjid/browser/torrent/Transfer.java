package com.sanjid.browser.torrent;

/** Immutable UI/host contract. Unknown totals and ETA are represented by -1. */
public final class Transfer {
    public enum Kind { HTTP, MEDIA, TORRENT, CONVERSION }
    public enum State { QUEUED, METADATA, CHECKING, DOWNLOADING, PAUSED, INTERRUPTED, COMPLETED, FAILED }
    public final String id, title;
    public final Kind kind;
    public final State state;
    public final long done, total, downRate, upRate;
    public final int peers, seeds;
    public Transfer(String id, String title, Kind kind, State state, long done, long total,
                    long downRate, long upRate, int peers, int seeds) {
        this.id=id; this.title=title; this.kind=kind; this.state=state;
        this.done=Math.max(0,done); this.total=total<0?-1:total;
        this.downRate=Math.max(0,downRate); this.upRate=Math.max(0,upRate);
        this.peers=Math.max(0,peers); this.seeds=Math.max(0,seeds);
    }
    public int percent() { return state==State.COMPLETED?100:total<=0?-1:(int)Math.min(100, Math.floor(100.0*done/total)); }
    public long etaSeconds() {
        if(state==State.COMPLETED) return 0;
        if(state!=State.DOWNLOADING||total<=0||downRate<=0) return -1;
        long remaining=Math.max(0,total-done);
        return remaining/downRate + (remaining%downRate==0?0:1);
    }
    public String key() { return kind.name()+":"+id; }
}
