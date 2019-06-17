package com.zyouke.replayingdecoder.client;

import com.zyouke.replayingdecoder.protocol.UserProtocolDecoder;
import com.zyouke.replayingdecoder.protocol.UserProtocolEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class CodecClientInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new UserProtocolDecoder());
        pipeline.addLast(new UserProtocolEncoder());
        pipeline.addLast(new CodecClientHandler());
        pipeline.addLast(new CodecClientHandler());
    }
}
















