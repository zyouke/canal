package com.zyouke.nio;

import java.io.UnsupportedEncodingException;

/**
 * 编解码
 */
public class Codec {
    private final static String lineBreak = "\r\n";
    public static String stringDecoding(StringBuilder builder, byte[] bytes){
        try{
            String body = new String(bytes, "UTF-8");
            if(lineBreak.equals(body)){
                return builder.toString();
            }else {
                return null;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }


}
