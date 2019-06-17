package com.zyouke.replayingdecoder.protocol;

import com.zyouke.replayingdecoder.protocol.UserProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

public class UserProtocolEncoder extends MessageToByteEncoder<UserProtocol> {

    @Override
    protected void encode(ChannelHandlerContext ctx, UserProtocol msg, ByteBuf out) throws Exception{
        out.writeInt(msg.getLength());
        out.writeBytes(msg.getContent());
    }
}
