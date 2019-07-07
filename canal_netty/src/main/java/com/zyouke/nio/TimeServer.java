package com.zyouke.nio;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.Key;
import java.util.Iterator;
import java.util.Set;

/**
 * @Author: zhoujun
 */
public class TimeServer {
    public static void main(String[] args) {
        TimeServer timeServer = new TimeServer();
        TimehandleThread timehandleThread = timeServer.new TimehandleThread(8080);
        timehandleThread.start();
    }



    class  TimehandleThread extends Thread{
        private StringBuilder builder = new StringBuilder();
        ServerSocketChannel serverSocketChannel;
        Selector selector ;
        public TimehandleThread(int port){
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
                    //select()阻塞到至少有一个通道在你注册的事件上就绪了
                    //如果没有准备好的channel,就在这一直阻塞
                    //select(long timeout)和select()一样，除了最长会阻塞timeout毫秒(参数)。
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
                        ByteBuffer readBuffer = ByteBuffer.allocate(10);
                        int readBytes = client.read(readBuffer);
                        if(readBytes > 0){
                            readBuffer.flip();
                            byte[] bytes = new byte[readBuffer.remaining()];
                            readBuffer.get(bytes);
                            String body = Codec.stringDecoding(builder, bytes);
                            if(StringUtils.isNoneBlank(body)){
                                System.out.println(String.format("接受请求数据 ：%s", body));
                                builder.delete(0,builder.length());
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
