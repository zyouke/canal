package com.zyouke.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class TimeClient {
    public static void main(String[] args){
         Selector selector;
         SocketChannel socketChannel;
        try {
            selector = Selector.open();
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress("127.0.0.1", 8080));
            socketChannel.register(selector,SelectionKey.OP_CONNECT);
            while (true){
                selector.select(1000);
                Iterator<SelectionKey> selectionKeyIterator = selector.selectedKeys().iterator();
                while (selectionKeyIterator.hasNext()){
                    SelectionKey selectionKey = selectionKeyIterator.next();
                    selectionKeyIterator.remove();
                    handleInput(selectionKey,selector);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleInput(SelectionKey selectionKey,Selector selector){
        if (selectionKey.isValid()) {
            try {
                SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                if (selectionKey.isConnectable()) {
                    if (socketChannel.finishConnect()) {
                        socketChannel.register(selector, SelectionKey.OP_WRITE);
                        selectionKey.interestOps(SelectionKey.OP_WRITE);
                    }else {
                        System.exit(1);
                    }
                }
                if (selectionKey.isWritable()) {
                    send(selectionKey,selector);
                }
                if (selectionKey.isReadable()) {
                    read(selectionKey);
                }
            } catch (IOException e) {
                e.printStackTrace();
                selectionKey.cancel();
                try {
                    selectionKey.channel().close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }


    private static void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        int len = channel.read(byteBuffer);
        if (len > 0) {
            byteBuffer.flip();
            byte[] byteArray = new byte[byteBuffer.limit()];
            byteBuffer.get(byteArray);
            System.out.println(new String(byteArray));
            len = channel.read(byteBuffer);
            byteBuffer.clear();
        }
        key.interestOps(SelectionKey.OP_READ);
    }

    private static void send(SelectionKey key,Selector selector) {
        SocketChannel channel = (SocketChannel) key.channel();

        for (int i = 0; i < 10; i++) {
            String ss = i + "Server ,how are you? this is package message from NioSocketClient!";
            int head = (ss).getBytes().length;
            ByteBuffer byteBuffer = ByteBuffer.allocate(4 + head);
            byteBuffer.put(intToBytes(head));
            byteBuffer.put(ss.getBytes());
            byteBuffer.flip();
            System.out.println("[client] send:" + i + "-- " + head + ss);
            while (byteBuffer.hasRemaining()) {
                try {
                    channel.write(byteBuffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            channel.register(selector, SelectionKey.OP_READ);
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }
    }

    /**
     * int到byte[]
     *
     * @param value
     * @return
     */
    private static byte[] intToBytes(int value) {
        byte[] result = new byte[4];
        // 由高位到低位
        result[0] = (byte) ((value >> 24) & 0xFF);
        result[1] = (byte) ((value >> 16) & 0xFF);
        result[2] = (byte) ((value >> 8) & 0xFF);
        result[3] = (byte) (value & 0xFF);
        return result;
    }
}
