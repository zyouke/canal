package com.zyouke.encoded_decod.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.SocketAddress;
import java.util.UUID;

public class EncodedDecodClientHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String message) throws Exception {
        System.out.println("服务端：" + ctx.channel().remoteAddress() + "的响应：" + message);
        ctx.channel().writeAndFlush("200 \r\n");
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println(print(ctx) + " 连接注册");
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println(print(ctx) + " 连接取消注册");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(print(ctx) + " 链接活跃中");
        ByteBuf byteBuf = Unpooled.buffer(10);
        byteBuf.writeInt(8);
        byteBuf.writeBytes(UUID.randomUUID().toString().getBytes("UTF-8"));
        ctx.writeAndFlush(byteBuf);
    }

    /**
     * 打印
     * @param channelHandlerContext
     * @return
     */
    public String print(ChannelHandlerContext channelHandlerContext) {
        Channel channel = channelHandlerContext.channel();
        SocketAddress localAddress = channel.localAddress();
        SocketAddress remoteAddress = channel.remoteAddress();
        return remoteAddress.toString().substring(1) + "<===>" + localAddress.toString().substring(1);
    }
}
