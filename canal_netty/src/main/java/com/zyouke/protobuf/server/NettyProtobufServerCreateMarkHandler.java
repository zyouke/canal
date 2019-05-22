package com.zyouke.protobuf.server;

import com.alibaba.otter.canal.protocol.CanalPacket;
import io.netty.channel.*;
import io.netty.util.AttributeKey;

import java.net.SocketAddress;

/**
 * 对AttributeKey Attribute AttributeMap的使用设置的Handler
 */
public class NettyProtobufServerCreateMarkHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception{
        ctx.channel().attr(AttributeKey.valueOf("chanal")).set(msg.toString());
        super.channelRead(ctx,msg);
    }
}
