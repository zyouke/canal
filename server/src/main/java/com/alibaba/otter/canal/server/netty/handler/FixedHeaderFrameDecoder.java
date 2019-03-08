package com.alibaba.otter.canal.server.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

/**
 * 解析对应的header信息
 * 
 * @author jianghang 2012-10-24 上午11:31:39
 * @version 1.0.0
 */
public class FixedHeaderFrameDecoder extends ReplayingDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        out.add(in.readLong());
    }
}
