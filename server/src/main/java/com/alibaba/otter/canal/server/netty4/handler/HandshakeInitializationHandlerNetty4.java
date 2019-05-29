package com.alibaba.otter.canal.server.netty4.handler;

import com.alibaba.otter.canal.protocol.CanalPacket;
import com.alibaba.otter.canal.protocol.CanalPacket.Handshake;
import com.alibaba.otter.canal.protocol.CanalPacket.Packet;
import com.alibaba.otter.canal.server.netty4.Netty4Utils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * handshake(握手)交互
 * 当客户端发送链接请求时服务端的处理器
 * @author jianghang 2012-10-24 上午11:39:54
 * @version 1.0.0
 */
public class HandshakeInitializationHandlerNetty4 extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(HandshakeInitializationHandlerNetty4.class);

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
        Netty4Utils.write(channel,body,null);
        logger.info("send handshake initialization packet to : {}", ctx.channel());
    }
}
