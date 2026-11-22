package com.sanjid.browser.torrent;
import java.util.*;
/** Host can merge independently owned Chromium and torrent records without ID collisions. */
public final class TransferIndex {
    private TransferIndex() {}
    public static List<Transfer> merge(List<Transfer>... sources) {
        Map<String,Transfer> result=new LinkedHashMap<>();
        for(List<Transfer> source:sources) for(Transfer item:source) result.put(item.key(),item);
        return Collections.unmodifiableList(new ArrayList<>(result.values()));
    }
    public static List<Transfer> filter(List<Transfer> items, Transfer.Kind kind, Transfer.State state) {
        List<Transfer> result=new ArrayList<>();
        for(Transfer item:items) if((kind==null||kind==item.kind)&&(state==null||state==item.state)) result.add(item);
        return Collections.unmodifiableList(result);
    }
}
