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
    public void nioSimpleTest(){
        try {
            TimeClient.TimeClientHandleThread timeClientHandleThread = TimeClient.start();
            SocketChannel socketChannel = timeClientHandleThread.getSocketChannel();
            System.out.println(socketChannel.getRemoteAddress().toString() + "连接成功");
            for (int i = 0; i < 100; i++) {
                ByteBuffer byteBuffer = ByteBuffer.allocate(20);
                byteBuffer.put(("zyouke" + i).getBytes());
                byteBuffer.flip();
                socketChannel.write(byteBuffer);
            }
            timeClientHandleThread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
