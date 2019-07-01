package com.alibaba.otter.canal.parse.driver.mysql;

import java.io.IOException;

import com.alibaba.otter.canal.parse.driver.mysql.socket.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.canal.parse.driver.mysql.packets.client.QueryCommandPacket;
import com.alibaba.otter.canal.parse.driver.mysql.packets.server.ErrorPacket;
import com.alibaba.otter.canal.parse.driver.mysql.packets.server.OKPacket;
import com.alibaba.otter.canal.parse.driver.mysql.utils.PacketManager;

/**
 * 默认输出的数据编码为UTF-8，如有需要请正确转码
 * 
 * @author jianghang 2013-9-4 上午11:51:11
 * @since 1.0.0
 */
public class MysqlUpdateExecutor {

    private static final Logger logger = LoggerFactory.getLogger(MysqlUpdateExecutor.class);

    private MysqlConnector connector;

    public MysqlUpdateExecutor(MysqlConnector connector) throws IOException {
        if (!connector.isConnected()) {
            throw new IOException("should execute connector.connect() first");
        }

        this.connector = connector;
    }

    /*
     * public MysqlUpdateExecutor(SocketChannel ch){ this.channel = ch; }
     */

    public OKPacket update(String updateString) throws IOException {
        QueryCommandPacket cmd = new QueryCommandPacket();
        cmd.setQueryString(updateString);
        byte[] bodyBytes = cmd.toBytes();
        SocketChannel channel = connector.getChannel();
        PacketManager.writeBody(channel, bodyBytes);
        byte[] body = PacketManager.readBytes(channel, PacketManager.readHeader(channel, 4).getPacketBodyLength());
        if (body[0] < 0) {
            ErrorPacket packet = new ErrorPacket();
            packet.fromBytes(body);
            throw new IOException(packet + "\n with command: " + updateString);
        }

        OKPacket packet = new OKPacket();
        packet.fromBytes(body);
        logger.info("read update result...{}",packet.toStringByFormat());
        return packet;
    }
}
