package com.alibaba.otter.canal.server.netty.handler;

import com.alibaba.otter.canal.protocol.CanalPacket;
import com.alibaba.otter.canal.protocol.CanalPacket.Handshake;
import com.alibaba.otter.canal.protocol.CanalPacket.Packet;
import com.alibaba.otter.canal.server.netty.NettyUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * handshake交互
 * 
 * @author jianghang 2012-10-24 上午11:39:54
 * @version 1.0.0
 */
public class HandshakeInitializationHandler extends SimpleChannelInboundHandler {

    // support to maintain socket channel.
    private ChannelGroup childGroups;

    public HandshakeInitializationHandler(ChannelGroup childGroups){
        this.childGroups = childGroups;
    }

    private static final Logger logger = LoggerFactory.getLogger(HandshakeInitializationHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx,Object o) throws Exception {
        // add new socket channel in channel container, used to manage sockets.
        if (childGroups != null) {
            childGroups.add(ctx.channel());
        }

        byte[] body = Packet.newBuilder()
            .setType(CanalPacket.PacketType.HANDSHAKE)
            .setBody(Handshake.newBuilder().build().toByteString())
            .build()
            .toByteArray();
        NettyUtils.write(ctx.channel(), body, null);
        logger.info("send handshake initialization packet to : {}", ctx.channel());
    }

}
