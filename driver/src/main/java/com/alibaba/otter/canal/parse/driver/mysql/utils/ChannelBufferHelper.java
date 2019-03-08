package com.alibaba.otter.canal.parse.driver.mysql.utils;

import com.alibaba.otter.canal.parse.driver.mysql.packets.HeaderPacket;
import com.alibaba.otter.canal.parse.driver.mysql.packets.IPacket;
import com.alibaba.otter.canal.parse.driver.mysql.packets.PacketWithHeaderPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ChannelBufferHelper {

    protected transient final Logger logger = LoggerFactory.getLogger(ChannelBufferHelper.class);

    public final HeaderPacket assembleHeaderPacket(ByteBuf byteBuf) {
        HeaderPacket header = new HeaderPacket();
        byte[] headerBytes = new byte[MSC.HEADER_PACKET_LENGTH];
        byteBuf.readBytes(headerBytes);
        header.fromBytes(headerBytes);
        return header;
    }

    public final PacketWithHeaderPacket assembleBodyPacketWithHeader(ByteBuf byteBuf, HeaderPacket header,PacketWithHeaderPacket body) throws IOException {
        if (body.getHeader() == null) {
            body.setHeader(header);
        }
        logger.debug("body packet type:{}", body.getClass());
        logger.debug("read body packet with packet length: {} ", header.getPacketBodyLength());
        byte[] packetBytes = new byte[header.getPacketBodyLength()];

        logger.debug("readable bytes before reading body:{}", byteBuf.readableBytes());
        byteBuf.readBytes(packetBytes);
        body.fromBytes(packetBytes);

        logger.debug("body packet: {}", body);
        return body;
    }

    public final ByteBuf createHeaderWithPacketNumberPlusOne(int bodyLength, byte packetNumber) {
        HeaderPacket header = new HeaderPacket();
        header.setPacketBodyLength(bodyLength);
        header.setPacketSequenceNumber((byte) (packetNumber + 1));
        return Unpooled.wrappedBuffer(header.toBytes());
    }

    public final ByteBuf createHeader(int bodyLength, byte packetNumber) {
        HeaderPacket header = new HeaderPacket();
        header.setPacketBodyLength(bodyLength);
        header.setPacketSequenceNumber(packetNumber);
        return Unpooled.wrappedBuffer(header.toBytes());
    }

    public final ByteBuf buildChannelBufferFromCommandPacket(IPacket packet) throws IOException {
        byte[] bodyBytes = packet.toBytes();
        ByteBuf header = createHeader(bodyBytes.length, (byte) 0);
        return Unpooled.wrappedBuffer(header, Unpooled.wrappedBuffer(bodyBytes));
    }
}
