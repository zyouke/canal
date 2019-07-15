package com.zyouke.nio;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

/**
 * 编解码
 */
public class Codec {

    private static final ByteBuffer cacheBuffer = ByteBuffer.allocate(100);
    private static boolean isCache = false;

    public static void decode(ByteBuffer byteBuffer){
        int bodyLen = -1;
        int head_length = 4;//数据包长度
        byte[] headByte = new byte[4];
        if (isCache) {
            cacheBuffer.flip();
            int cacheBufferLength = cacheBuffer.remaining();
            byteBuffer.flip();
            byte[] array = byteBuffer.array();
            int byteTotal = cacheBufferLength + array.length;
            if (byteTotal > byteBuffer.capacity()){
                byteBuffer = ByteBuffer.allocate(byteTotal);
            }
            byteBuffer.put(cacheBuffer).put(array);
        }
        byteBuffer.flip();
        while (byteBuffer.remaining() > 0) {
            if (bodyLen == -1) {// 还没有读出包头，先读出包头
                if (byteBuffer.remaining() >= head_length) {// 可以读出包头，否则缓存
                    byteBuffer.mark();
                    byteBuffer.get(headByte);
                    bodyLen = Codec.byteArrayToInt(headByte);
                } else {
                    byteBuffer.reset();
                    isCache = true;
                    cacheBuffer.clear();
                    cacheBuffer.put(byteBuffer);
                    break;
                }
            } else {// 已经读出包头
                if (byteBuffer.remaining() >= bodyLen) {// 大于等于一个包，否则缓存
                    byte[] bodyByte = new byte[bodyLen];
                    byteBuffer.get(bodyByte, 0, bodyLen);
                    bodyLen = -1;
                    System.out.println("receive from clien content is:" + new String(bodyByte));
                } else {
                    byteBuffer.reset();
                    cacheBuffer.clear();
                    cacheBuffer.put(byteBuffer);
                    isCache = true;
                    break;
                }
            }
        }
    }






    /**
     * byte[]转int
     *
     * @param bytes
     * @return
     */
    public static int byteArrayToInt(byte[] bytes) {
        int value = 0;
        // 由高位到低位
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (bytes[i] & 0x000000FF) << shift;// 往高位游
        }
        return value;
    }

}
