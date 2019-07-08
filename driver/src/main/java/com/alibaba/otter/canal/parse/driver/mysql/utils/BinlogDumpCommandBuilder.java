package com.alibaba.otter.canal.parse.driver.mysql.utils;

import com.alibaba.otter.canal.parse.driver.mysql.packets.HeaderPacket;
import com.alibaba.otter.canal.parse.driver.mysql.packets.client.BinlogDumpCommandPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

public class BinlogDumpCommandBuilder {

    public BinlogDumpCommandPacket build(String binglogFile, long position, long slaveId) {
        BinlogDumpCommandPacket command = new BinlogDumpCommandPacket();
        command.binlogPosition = position;
        if (!StringUtils.isEmpty(binglogFile)) {
            command.binlogFileName = binglogFile;
        }
        command.slaveServerId = slaveId;
        // end settings.
        return command;
    }

    public ByteBuf toChannelBuffer(BinlogDumpCommandPacket command) throws IOException {
        byte[] commandBytes = command.toBytes();
        byte[] headerBytes = assembleHeaderBytes(commandBytes.length);
        ByteBuf byteBuf = Unpooled.wrappedBuffer(headerBytes, commandBytes);
        return byteBuf;
    }

    private byte[] assembleHeaderBytes(int length) {
        HeaderPacket header = new HeaderPacket();
        header.setPacketBodyLength(length);
        header.setPacketSequenceNumber((byte) 0x00);
        return header.toBytes();
    }
}
