package com.zyouke.netty.simple;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;

import java.util.ArrayList;
import java.util.List;

public class HelloWorldClientHandler extends SimpleChannelInboundHandler<String> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println("服务端响应数据 ： " + msg);
        Thread.sleep(1000);
        ctx.channel().writeAndFlush("HelloWorld \r\n");
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("与远程服务请求注册。。。。。");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("与远程服务 ：" + ctx.channel().remoteAddress() + "连接处于活跃");
        ctx.channel().writeAndFlush("HelloWorld\r\n");
    }
}
