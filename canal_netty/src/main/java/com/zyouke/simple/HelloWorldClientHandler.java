package com.zyouke.simple;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;

public class HelloWorldClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("服务端响应数据 ： " + msg);
        ctx.channel().writeAndFlush("HelloWorld \r\n");
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("与远程服务请求注册。。。。。");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("与远程服务 ：" + ctx.channel().remoteAddress() + "连接处于活跃");
        ByteBuf buffer = Unpooled.buffer();
        System.out.println(buffer.toString());
        ctx.channel().writeAndFlush(buffer);
    }
}
