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
    private static final ByteBuffer cacheBuffer = ByteBuffer.allocate(100);
    private static boolean isCache = false;

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
                else if (selectionKey.isWritable()) {
                    reply(selectionKey);
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

    private static void reply(SelectionKey selectionKey) {
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(100);
        byte[] bytes = "pingaaaaaaaaaa".getBytes();
        byteBuffer.put(bytes);
        try {
            channel.write(byteBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 一个client的write事件不一定唯一对应server的read事件，所以需要缓存不完整的包，以便拼接成完整的包
    //包协议：包=包头(4byte)+包体，包头内容为包体的数据长度
    private static void read(SelectionKey selectionKey) {
        int head_length = 4;//数据包长度
        byte[] headByte = new byte[4];
        try {
            SocketChannel channel = (SocketChannel) selectionKey.channel();
            ByteBuffer byteBuffer = ByteBuffer.allocate(100);
            int bodyLen = -1;
            if (isCache) {
                cacheBuffer.flip();
                byteBuffer.put(cacheBuffer);
            }
            channel.read(byteBuffer);// 当前read事件
            byteBuffer.flip();// write mode to read mode
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
                        selectionKey.interestOps(SelectionKey.OP_WRITE);
                    } else {
                        byteBuffer.reset();
                        cacheBuffer.clear();
                        cacheBuffer.put(byteBuffer);
                        isCache = true;
                        break;
                    }
                }
            }
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
