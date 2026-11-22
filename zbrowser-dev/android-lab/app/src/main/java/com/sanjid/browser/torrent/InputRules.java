package com.sanjid.browser.torrent;
import java.io.*;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
final class InputRules {
    private InputRules() {}
    static boolean validMagnet(String s) {
        if(s==null||s.length()>16384) return false;
        try {
            URI u=new URI(s);
            if(!"magnet".equalsIgnoreCase(u.getScheme()))return false;
            String part=u.getRawSchemeSpecificPart();
            if(part==null||!part.startsWith("?"))return false;
            for(String pair:part.substring(1).split("&")){
                if(!pair.startsWith("xt="))continue;
                String xt=URLDecoder.decode(pair.substring(3),StandardCharsets.UTF_8.name());
                if(xt.matches("(?i)urn:btih:([0-9a-f]{40}|[a-z2-7]{32})")||xt.matches("(?i)urn:btmh:1220[0-9a-f]{64}"))return true;
            }
        } catch(Exception ignored){}
        return false;
    }
    static byte[] readLimited(InputStream input,int max) throws IOException {
        ByteArrayOutputStream out=new ByteArrayOutputStream(); byte[] b=new byte[8192]; int n;
        while((n=input.read(b))!=-1){ if(out.size()>max-n) throw new IOException("Metadata exceeds size limit"); out.write(b,0,n); }
        return out.toByteArray();
    }
    static boolean inside(File root,File child)throws IOException {return child.getCanonicalPath().startsWith(root.getCanonicalPath()+File.separator);}
}
