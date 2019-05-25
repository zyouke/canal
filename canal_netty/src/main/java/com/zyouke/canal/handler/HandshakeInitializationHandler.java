package com.zyouke.canal.handler;

import com.alibaba.otter.canal.protocol.CanalPacket;
import com.alibaba.otter.canal.protocol.CanalPacket.Handshake;
import com.alibaba.otter.canal.protocol.CanalPacket.Packet;
import com.alibaba.otter.canal.server.netty.NettyUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * handshake(握手)交互
 * 当客户端发送链接请求时服务端的处理器
 * @author jianghang 2012-10-24 上午11:39:54
 * @version 1.0.0
 */
public class HandshakeInitializationHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(HandshakeInitializationHandler.class);

    /**
     * 当客户端发送链接申请时服务端给应答
     * @param ctx
     */
    @Override
    public void channelRegistered(ChannelHandlerContext ctx){
        Channel channel = ctx.channel();
        byte[] body = Packet.newBuilder()
                .setType(CanalPacket.PacketType.HANDSHAKE)
                .setBody(Handshake.newBuilder().build().toByteString())
                .build()
                .toByteArray();
        channel.writeAndFlush(body);
        logger.info("send handshake initialization packet to : {}", ctx.channel());
    }
}
