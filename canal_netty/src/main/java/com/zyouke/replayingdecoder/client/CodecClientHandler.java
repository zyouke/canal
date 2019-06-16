package com.zyouke.replayingdecoder.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class CodecClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
        System.out.println("服务端：" + ctx.channel().remoteAddress() + "的响应：" + message);
        ctx.channel().writeAndFlush("200");
    }

}
