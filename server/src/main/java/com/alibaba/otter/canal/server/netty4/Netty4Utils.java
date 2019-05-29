package com.alibaba.otter.canal.server.netty4;

import com.alibaba.otter.canal.protocol.CanalPacket;
import com.alibaba.otter.canal.protocol.CanalPacket.Ack;
import com.alibaba.otter.canal.protocol.CanalPacket.Packet;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Netty4Utils {

    private static final Logger logger = LoggerFactory.getLogger(Netty4Utils.class);
    private static int HEADER_LENGTH = 4;

    public static void write(Channel channel, byte[] body, ChannelFutureListener channelFutureListner){
        byte[] header = ByteBuffer.allocate(HEADER_LENGTH).order(ByteOrder.BIG_ENDIAN).putInt(body.length).array();
        if(channelFutureListner == null){
            channel.writeAndFlush(Unpooled.wrappedBuffer(header, body));
        }else{
            channel.writeAndFlush(Unpooled.wrappedBuffer(header, body)).addListener(channelFutureListner);
        }
    }

    public static void ack(Channel channel, ChannelFutureListener channelFutureListner){
        write(channel, Packet.newBuilder().setType(CanalPacket.PacketType.ACK).setBody(Ack.newBuilder().build().toByteString()).build().toByteArray(), channelFutureListner);
    }

    public static void error(int errorCode, String errorMessage, Channel channel, ChannelFutureListener channelFutureListener){
        if(channelFutureListener == null){
            channelFutureListener = ChannelFutureListener.CLOSE;
        }

        logger.error("ErrotCode:{} , Caused by : \n{}", errorCode, errorMessage);
        write(channel, Packet.newBuilder().setType(CanalPacket.PacketType.ACK).setBody(Ack.newBuilder().setErrorCode(errorCode).setErrorMessage(errorMessage).build().toByteString()).build().toByteArray(), channelFutureListener);
    }
}
