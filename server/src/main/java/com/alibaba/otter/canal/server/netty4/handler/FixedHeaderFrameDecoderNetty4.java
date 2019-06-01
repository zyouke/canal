package com.alibaba.otter.canal.server.netty4.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;

import java.util.List;


/**
 * 解析对应的header信息
 * 
 * @author jianghang 2012-10-24 上午11:31:39
 * @version 1.0.0
 */
public class FixedHeaderFrameDecoderNetty4 extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out)throws Exception {
        byteBuf.readBytes(byteBuf.readInt());
    }
}
