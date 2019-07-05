package com.zyouke.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @Author: zhoujun
 */
public class NioServer {
    public static void main(String[] args) {
        ServerSocketChannel serverSocketChannel;
        Selector selector = null;
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(8080));
            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                //select()阻塞到至少有一个通道在你注册的事件上就绪了
                //如果没有准备好的channel,就在这一直阻塞
                //select(long timeout)和select()一样，除了最长会阻塞timeout毫秒(参数)。
                selector.select();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
            //返回已经就绪的SelectionKey，然后迭代执行
            Set<SelectionKey> selectionKeySet = selector.selectedKeys();
            Iterator<SelectionKey> selectionKeyIterator = selectionKeySet.iterator();
            while (selectionKeyIterator.hasNext()) {
                SelectionKey key = selectionKeyIterator.next();
                selectionKeyIterator.remove();
                try {
                    if (key.isAcceptable()) {
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        SocketChannel client = server.accept();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);
                    } else if (key.isReadable()){
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(10);
                        int read = client.read(buffer);
                        byte[] array = buffer.array();
                        System.out.println(new String(array));
                        ByteBuffer writeBuffer = ByteBuffer.wrap(String.valueOf(System.currentTimeMillis()).getBytes());
                        client.write(writeBuffer);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    key.cancel();
                    try {
                        key.channel().close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }
}
