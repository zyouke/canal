package com.alibaba.otter.canal.client.running;

import com.alibaba.otter.canal.protocol.CanalPacket;
import com.google.protobuf.ByteString;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

public class SimpleCanalConnectorTest{
    private SocketChannel channel;
    private ReadableByteChannel readableChannel;
    private WritableByteChannel writableChannel;
    // 读写数据分别使用不同的锁进行控制，减小锁粒度,读也需要排他锁，并发度容易造成数据包混乱，反序列化失败
    private Object readDataLock = new Object();
    private Object writeDataLock = new Object();
    private final ByteBuffer readHeader = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
    private final ByteBuffer writeHeader = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);

    @Before
    public void before(){
        try{
            channel = SocketChannel.open();
            channel.socket().setSoTimeout(3000);
            SocketAddress address = new InetSocketAddress("127.0.0.1",11111);
            channel.connect(address);
            readableChannel = Channels.newChannel(channel.socket().getInputStream());
            writableChannel = Channels.newChannel(channel.socket().getOutputStream());
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @Test
    public void handshakeTest(){
        try{
            CanalPacket.Packet packet = CanalPacket.Packet.parseFrom(readNextPacket());
            System.out.println("type : " + packet.getType().toString());
            System.out.println("body : " + CanalPacket.Handshake.parseFrom(packet.getBody()).toString());
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    @Test
    public void clientAuthTest(){
        try{
            CanalPacket.ClientAuth clientAuth = CanalPacket.ClientAuth.newBuilder().
                                         setUsername("canal").
                                         setPassword(ByteString.copyFromUtf8("123456")).
                                         setNetReadTimeout(3000).
                                         setNetWriteTimeout(3000).
                                         build();
            writeWithHeader(CanalPacket.Packet.newBuilder().
                            setType(CanalPacket.PacketType.CLIENTAUTHENTICATION).
                            setBody(clientAuth.toByteString()).build().toByteArray());
            CanalPacket.Packet packet = CanalPacket.Packet.parseFrom(readNextPacket());
        }catch(IOException e){
            e.printStackTrace();
        }
    }


    // ==================== helper method start====================

    private void writeWithHeader(byte[] body) throws IOException{
        writeWithHeader(writableChannel, body);
    }

    private byte[] readNextPacket() throws IOException{
        return readNextPacket(readableChannel);
    }

    private void writeWithHeader(WritableByteChannel channel, byte[] body) throws IOException{
        synchronized(writeDataLock){
            writeHeader.clear();
            writeHeader.putInt(body.length);
            writeHeader.flip();
            channel.write(writeHeader);
            channel.write(ByteBuffer.wrap(body));
        }
    }

    private byte[] readNextPacket(ReadableByteChannel channel) throws IOException{
        synchronized(readDataLock){
            readHeader.clear();
            read(channel, readHeader);
            int bodyLen = readHeader.getInt(0);
            ByteBuffer bodyBuf = ByteBuffer.allocate(bodyLen).order(ByteOrder.BIG_ENDIAN);
            read(channel, bodyBuf);
            return bodyBuf.array();
        }
    }

    private void read(ReadableByteChannel channel, ByteBuffer buffer) throws IOException{
        while(buffer.hasRemaining()){
            int r = channel.read(buffer);
            if(r == -1){
                throw new IOException("end of stream when reading header");
            }
        }
    }

}
