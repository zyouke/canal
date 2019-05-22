package com.zyouke.protobuf.server;

import com.alibaba.otter.canal.protocol.CanalPacket;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.SocketAddress;

public class NettyProtobufServerHandler extends SimpleChannelInboundHandler<CanalPacket.Handshake> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CanalPacket.Handshake handshake) throws Exception {
        System.out.println("NettyProtobufServerHandler 当前传递上下文的对象 " + ctx.toString());
        Channel channel = ctx.channel();
        System.out.println("接受客户端" + channel.remoteAddress() + "的请求：" + handshake);
        CanalPacket.Handshake.Builder builder = CanalPacket.Handshake.newBuilder();
        builder.addSupportedCompressions(CanalPacket.Compression.LZF);
        System.out.println("获取att的数据 ：" + channel.attr(AttributeKey.valueOf("chanal")).get());
        channel.writeAndFlush(builder).addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception{
                System.out.println("写数据成功后回调。。。。。。。。。");
            }
        });
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
