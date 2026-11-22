package com.sanjid.browser.torrent;
import java.util.*;
import java.io.*;
public final class ContractTest {
    static void check(boolean b){if(!b)throw new AssertionError();}
    public static void main(String[] a)throws Exception{
        Transfer t=new Transfer("1","Linux",Transfer.Kind.TORRENT,Transfer.State.DOWNLOADING,25,100,8,2,3,2);
        check(t.percent()==25); check(t.etaSeconds()==10);
        Transfer h=new Transfer("1","PDF",Transfer.Kind.HTTP,Transfer.State.DOWNLOADING,0,-1,0,0,0,0);
        check(h.percent()==-1&&h.etaSeconds()==-1);
        check(TransferIndex.merge(List.of(t),List.of(h)).size()==2);
        check(TransferIndex.merge(List.of(t),List.of(t)).size()==1);
        check(TransferIndex.filter(List.of(t,h),Transfer.Kind.TORRENT,null).size()==1);
        check(InputRules.validMagnet("magnet:?xt=urn:btih:0123456789abcdef0123456789abcdef01234567"));
        check(InputRules.validMagnet("magnet:?xt=urn:btmh:1220"+"a".repeat(64)));
        check(!InputRules.validMagnet("https://example.org/test.torrent"));
        check(!InputRules.validMagnet("magnet:?dn=missing"));
        check(InputRules.readLimited(new ByteArrayInputStream(new byte[10]),10).length==10);
        try {InputRules.readLimited(new ByteArrayInputStream(new byte[11]),10);throw new AssertionError();}catch(IOException expected){}
        check(!InputRules.inside(new File("/tmp/root"),new File("/tmp/root/../escape")));
        System.out.println("PASS: 12 host contract assertions (no native engine or Android runtime tested)");
    }
}
