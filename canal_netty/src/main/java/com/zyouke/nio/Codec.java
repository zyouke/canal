package com.zyouke.nio;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * 编解码
 */
public class Codec {
    public static void decoding(ByteBuffer cacheBuffer, ByteBuffer readBuffer,boolean isCache){
        int bodyLen = -1;
        int head_length = 4;//数据包长度
        byte[] headByte = new byte[head_length];
        while (readBuffer.remaining() > 0) {
            if (bodyLen == -1) {// 还没有读出包头，先读出包头
                if (readBuffer.remaining() >= head_length) {// 可以读出包头，否则缓存
                    readBuffer.mark();
                    readBuffer.get(headByte);
                    bodyLen = byteArrayToInt(headByte);
                } else {
                    readBuffer.reset();
                    isCache = true;
                    cacheBuffer.clear();
                    cacheBuffer.put(readBuffer);
                    break;
                }
            } else {// 已经读出包头
                if (readBuffer.remaining() >= bodyLen) {// 大于等于一个包，否则缓存
                    byte[] bodyByte = new byte[bodyLen];
                    readBuffer.get(bodyByte, 0, bodyLen);
                    bodyLen = -1;
                } else {
                    readBuffer.reset();
                    cacheBuffer.clear();
                    cacheBuffer.put(readBuffer);
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
    private static int byteArrayToInt(byte[] bytes) {
        int value = 0;
        // 由高位到低位
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (bytes[i] & 0x000000FF) << shift;// 往高位游
        }
        return value;
    }

}
