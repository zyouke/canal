package com.zyouke.encoded_decod.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.SocketAddress;
import java.util.UUID;

public class EncodedDecodClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
        System.out.println("服务端：" + ctx.channel().remoteAddress() + "的响应：" + message);
        ctx.channel().writeAndFlush("200");
    }
}
