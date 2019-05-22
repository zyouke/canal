package com.zyouke.canal.handler;

import com.alibaba.otter.canal.protocol.CanalPacket;
import com.alibaba.otter.canal.protocol.CanalPacket.Handshake;
import com.alibaba.otter.canal.protocol.CanalPacket.Packet;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * handshake交互
 * 
 * @author jianghang 2012-10-24 上午11:39:54
 * @version 1.0.0
 */
public class HandshakeInitializationHandler extends SimpleChannelInboundHandler<Packet> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet){
        Channel channel = ctx.channel();
        packet.toBuilder()
              .setType(CanalPacket.PacketType.HANDSHAKE)
              .setBody(Handshake.newBuilder().build().toByteString());
        channel.writeAndFlush(packet);
    }
}
