package com.zyouke.protobuf.client;

import com.alibaba.otter.canal.protocol.CanalPacket;
import com.google.protobuf.ByteString;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class NettyProtobufClientHandler extends SimpleChannelInboundHandler<CanalPacket.Handshake> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CanalPacket.Handshake handshake) throws Exception {
        System.out.println("服务端响应数据 ： " + handshake);
        CanalPacket.Handshake.Builder builder = CanalPacket.Handshake.newBuilder();
        builder.setSeeds(ByteString.copyFromUtf8("200"));
        //ctx.channel().writeAndFlush(builder);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("与远程服务请求注册。。。。。");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("与远程服务 ：" + ctx.channel().remoteAddress() + "连接处于活跃");
        CanalPacket.Handshake.Builder builder = CanalPacket.Handshake.newBuilder();
        builder.setSeeds(ByteString.copyFromUtf8("3"));
        builder.setCommunicationEncoding("UTF-8");
        builder.addSupportedCompressions(CanalPacket.Compression.GZIP);
        builder.build();
        ctx.channel().writeAndFlush(builder);
    }
}
