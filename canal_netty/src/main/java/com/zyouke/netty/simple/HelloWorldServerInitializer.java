package com.zyouke.netty.simple;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;


public class HelloWorldServerInitializer extends ChannelInitializer<SocketChannel>{
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new DelimiterBasedFrameDecoder(1000,Delimiters.lineDelimiter()));
        pipeline.addLast(new StringEncoder());
        pipeline.addLast(new StringDecoder());
        pipeline.addLast(new HelloWorldServerHandler());
    }
}
