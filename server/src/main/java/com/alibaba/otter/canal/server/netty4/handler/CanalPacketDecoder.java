package com.alibaba.otter.canal.server.netty4.handler;

import com.alibaba.otter.canal.protocol.CanalPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;


/**
 * CanalPacket 使用的协议是protocol
 * 该类是将byteBuf解析成CanalPacket
 * @author jianghang 2012-10-24 上午11:31:39
 * @version 1.0.0
 */
public class CanalPacketDecoder extends ReplayingDecoder<Void> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out)throws Exception {
        byte[] bytes = new byte[byteBuf.readInt()];
        byteBuf.readBytes(bytes);
        CanalPacket.Packet packet = CanalPacket.Packet.parseFrom(bytes);
        out.add(packet);
    }
}
