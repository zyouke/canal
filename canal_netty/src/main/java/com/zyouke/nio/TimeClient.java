package com.zyouke.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class TimeClient {
    public static void main(String[] args) throws Exception{
        TimeClient.start();
    }

    public static TimeClientHandleThread start(){
        TimeClient timeClient = new TimeClient();
        TimeClientHandleThread timeClientHandleThread = timeClient.new TimeClientHandleThread();
        timeClientHandleThread.start();
        while (!timeClientHandleThread.isConnectSuccess){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return timeClientHandleThread;
    }
    class  TimeClientHandleThread extends Thread{
        private Selector selector;
        private SocketChannel socketChannel;
        private boolean isConnectSuccess = false;
        public TimeClientHandleThread() {
            try {
                selector = Selector.open();
                socketChannel = SocketChannel.open();
                socketChannel.configureBlocking(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public boolean isConnectSuccess() {
            return isConnectSuccess;
        }

        public void setConnectSuccess(boolean connectSuccess) {
            isConnectSuccess = connectSuccess;
        }

        public SocketChannel getSocketChannel() {
            return socketChannel;
        }

        public void setSocketChannel(SocketChannel socketChannel) {
            this.socketChannel = socketChannel;
        }

        @Override
        public void run() {
            try {
                boolean isConnect = socketChannel.connect(new InetSocketAddress("127.0.0.1", 8080));
                if (isConnect){
                    this.isConnectSuccess = true;
                    socketChannel.register(selector, SelectionKey.OP_READ);
                    ByteBuffer byteBuffer = ByteBuffer.allocate(100);
                    byteBuffer.put("ping".getBytes());
                    byteBuffer.flip();
                    socketChannel.write(byteBuffer);
                }else {
                    socketChannel.register(selector,SelectionKey.OP_CONNECT);
                }
                while (true){
                    selector.select(1000);
                    Iterator<SelectionKey> selectionKeyIterator = selector.selectedKeys().iterator();
                    while (selectionKeyIterator.hasNext()){
                        SelectionKey selectionKey = selectionKeyIterator.next();
                        selectionKeyIterator.remove();
                        handleInput(selectionKey);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void handleInput(SelectionKey selectionKey){
            if (selectionKey.isValid()) {
                try {
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                    if (selectionKey.isConnectable()) {
                        if (socketChannel.finishConnect()) {
                            this.isConnectSuccess = true;
                            socketChannel.register(selector, SelectionKey.OP_READ);
                            ByteBuffer byteBuffer = ByteBuffer.allocate(100);
                            byteBuffer.put("ping".getBytes());
                            byteBuffer.flip();
                            socketChannel.write(byteBuffer);
                        }else {
                            System.exit(1);
                        }
                    }
                    if (selectionKey.isReadable()) {
                        SocketChannel client = (SocketChannel) selectionKey.channel();
                        ByteBuffer readBuffer = ByteBuffer.allocate(100);
                        int readBytes = client.read(readBuffer);
                        if (readBytes > 0) {
                            readBuffer.flip();
                            byte[] bytes = new byte[readBuffer.remaining()];
                            readBuffer.get(bytes);
                            String body = new String(bytes, "UTF-8");
                            System.out.println(String.format("接收服务端数据 : %s", body));
                            /*SocketAddress localAddress = socketChannel.getLocalAddress();
                            ByteBuffer byteBuffer = ByteBuffer.allocate(100);
                            byteBuffer.put(localAddress.toString().getBytes());
                            byteBuffer.flip();
                            socketChannel.write(byteBuffer);*/
                        }
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
    }
}
