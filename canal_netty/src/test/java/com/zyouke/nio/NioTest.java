package com.zyouke.nio;

import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @Author: zhoujun
 */
public class NioTest {

    @Test
    public void nioSimpleTest(){}

    @Test
    public void byteBufferFlipTest(){
        ByteBuffer byteBuffer = ByteBuffer.allocate(10);
        byte[] bytes = new byte[]{1,2,3,4,5};
        byteBuffer.put(bytes);
        System.out.println("limit:" + byteBuffer.limit());
        System.out.println("position:" + byteBuffer.position());
        System.out.println("mark:" + byteBuffer.mark());
        System.out.println("---------flip-------------");
        byteBuffer.flip();
        System.out.println("limit:" + byteBuffer.limit());
        System.out.println("position:" + byteBuffer.position());
        System.out.println("mark:" + byteBuffer.mark());
    }

}
