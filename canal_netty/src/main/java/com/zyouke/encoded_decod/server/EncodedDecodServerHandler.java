package com.zyouke.encoded_decod.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.net.SocketAddress;
import java.util.List;
import java.util.UUID;

public class EncodedDecodServerHandler extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception{
        int readInt = in.readInt();
        System.out.println(readInt);
        ctx.writeAndFlush(UUID.randomUUID().toString());
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
