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
        TimeServer timeServer = new TimeServer();
        TimeServerHandleThread timehandleThread = timeServer.new TimeServerHandleThread(8080);
        timehandleThread.start();
    }

    class  TimeServerHandleThread extends Thread{
        ServerSocketChannel serverSocketChannel;
        Selector selector ;
        public TimeServerHandleThread(int port){
            try{
                serverSocketChannel = ServerSocketChannel.open();
                serverSocketChannel.configureBlocking(false);
                serverSocketChannel.socket().bind(new InetSocketAddress(port));
                selector = Selector.open();
                serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
                System.out.println(TimeServer.class.getSimpleName() + "在端口" + port +"上启动");
            }catch(IOException e){
                e.printStackTrace();
                System.exit(1);
            }
        }

        @Override
        public void run(){
            while(true){
                try {
                    selector.select(5000);
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
                //返回已经就绪的SelectionKey，然后迭代执行
                Set<SelectionKey> selectionKeySet = selector.selectedKeys();
                Iterator<SelectionKey> selectionKeyIterator = selectionKeySet.iterator();
                while (selectionKeyIterator.hasNext()) {
                    SelectionKey selectionKey = selectionKeyIterator.next();
                    selectionKeyIterator.remove();
                    try{
                        handleInput(selectionKey);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }

            }
        }
        private void handleInput(SelectionKey selectionKey) throws Exception{
            if(selectionKey.isValid()){
                try {
                    if (selectionKey.isAcceptable()) {
                        ServerSocketChannel server = (ServerSocketChannel) selectionKey.channel();
                        SocketChannel client = server.accept();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);
                    } else if (selectionKey.isReadable()){
                        SocketChannel client = (SocketChannel) selectionKey.channel();
                        ByteBuffer readBuffer = ByteBuffer.allocate(100);
                        int readBytes = client.read(readBuffer);
                        if(readBytes > 0){
                            readBuffer.flip();
                            byte[] bytes = new byte[readBuffer.remaining()];
                            readBuffer.get(bytes);
                            String body = new String(bytes, "UTF-8");
                            if ("ping".equals(body)){
                                System.out.println(String.format("客户端 : %s %s",client.getRemoteAddress().toString(),"请求连接"));
                                Thread.sleep(3000);
                                ByteBuffer writeBuffer = ByteBuffer.wrap("pong".getBytes());
                                client.write(writeBuffer);
                            }else {
                                if (isCache) {
                                    cacheBuffer.flip();
                                    readBuffer.put(cacheBuffer);
                                }
                                Codec.decoding(cacheBuffer,readBuffer,isCache);
                                selectionKey.interestOps(SelectionKey.OP_READ);
                                System.out.println(String.format("接收客户端请求数据 : %s",body));
                                ByteBuffer writeBuffer = ByteBuffer.wrap(String.valueOf(System.currentTimeMillis()).getBytes());
                                client.write(writeBuffer);
                            }
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
