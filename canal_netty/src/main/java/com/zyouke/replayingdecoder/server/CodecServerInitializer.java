package com.zyouke.replayingdecoder.server;

import com.zyouke.replayingdecoder.protocol.UserProtocolDecoder;
import com.zyouke.replayingdecoder.protocol.UserProtocolEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;


public class CodecServerInitializer extends ChannelInitializer<SocketChannel>{
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new UserProtocolDecoder());
        pipeline.addLast(new UserProtocolEncoder());
        pipeline.addLast(new CodecServerHandler());
    }
}
