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
public class TimeServer {
    public static void main(String[] args) {
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(8080));
            Selector selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println(TimeServer.class.getSimpleName() + "在端口8080上启动");
            while (true) {
                selector.select(5000);
                //返回已经就绪的SelectionKey，然后迭代执行
                Set<SelectionKey> selectionKeySet = selector.selectedKeys();
                Iterator<SelectionKey> selectionKeyIterator = selectionKeySet.iterator();
                while (selectionKeyIterator.hasNext()) {
                    SelectionKey selectionKey = selectionKeyIterator.next();
                    selectionKeyIterator.remove();
                    handleInput(selectionKey, selector);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void handleInput(SelectionKey selectionKey, Selector selector) throws Exception {
        if (selectionKey.isValid()) {
            try {
                if (selectionKey.isAcceptable()) {
                    ServerSocketChannel server = (ServerSocketChannel) selectionKey.channel();
                    SocketChannel client = server.accept();
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ);
                } else if (selectionKey.isReadable()) {
                    read(selectionKey);
                }
            } catch (IOException e) {
                e.printStackTrace();
                selectionKey.cancel();
                try {
                    selectionKey.channel().close();
                } catch (IOException ee) {
                    ee.printStackTrace();
                }
            }
        }
    }

    private static void reply(SocketChannel channel) {
        byte[] bytes = "ping".getBytes();
        ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
        byteBuffer.put(bytes);
        byteBuffer.flip();
        try {
            channel.write(byteBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 一个client的write事件不一定唯一对应server的read事件，所以需要缓存不完整的包，以便拼接成完整的包
    //包协议：包=包头(4byte)+包体，包头内容为包体的数据长度
    private static void read(SelectionKey selectionKey) {
        try {
            SocketChannel channel = (SocketChannel) selectionKey.channel();
            ByteBuffer byteBuffer = ByteBuffer.allocate(50);
            channel.read(byteBuffer);// 当前read事件
            Codec.decode(byteBuffer);
            selectionKey.interestOps(SelectionKey.OP_READ);
        } catch (IOException e) {
            try {
                selectionKey.cancel();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }

    }

}
