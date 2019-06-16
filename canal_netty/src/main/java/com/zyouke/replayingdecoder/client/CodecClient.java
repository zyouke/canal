package com.zyouke.replayingdecoder.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.UUID;

public class CodecClient {

    public static void main(String[] args) {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group).
        channel(NioSocketChannel.class).
        option(ChannelOption.SO_KEEPALIVE,false).
        handler(new LoggingHandler(LogLevel.INFO)).
        handler(new CodecClientInitializer());
        try {
            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 8080).sync();
            Channel channel = channelFuture.channel();
            channel.writeAndFlush(UUID.randomUUID().toString());
            channel.writeAndFlush("\r\n");
            channel.closeFuture();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

}
