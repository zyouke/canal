package com.zyouke.netty.protobuf;

import com.alibaba.otter.canal.protocol.CanalPacket;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.SocketAddress;

public class NettyProtobufServerHandler extends SimpleChannelInboundHandler<CanalPacket.Handshake> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CanalPacket.Handshake handshake) throws Exception {
        System.out.println("接受客户端" + ctx.channel().remoteAddress() + "的请求：" + handshake);
        CanalPacket.Handshake.Builder builder = CanalPacket.Handshake.newBuilder();
        builder.addSupportedCompressions(CanalPacket.Compression.LZF);
        ctx.channel().writeAndFlush(builder);
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
