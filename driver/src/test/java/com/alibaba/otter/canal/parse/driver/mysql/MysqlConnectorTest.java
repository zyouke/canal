package com.alibaba.otter.canal.parse.driver.mysql;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;

import com.alibaba.otter.canal.parse.driver.mysql.packets.server.ResultSetPacket;

public class MysqlConnectorTest {

    MysqlConnector connector = new MysqlConnector(new InetSocketAddress("122.114.90.68", 3306), "canal", "canal");
    {
        try {
            connector.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void testQuery() {
        try {
            MysqlQueryExecutor executor = new MysqlQueryExecutor(connector);
            ResultSetPacket result =  executor.query("select * from canal.canal where id = 30");
            System.out.println(result);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        } finally {
            try {
                connector.disconnect();
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }
    }

    @Test
    public void testUpdate() {
        try {
            MysqlUpdateExecutor executor = new MysqlUpdateExecutor(connector);
            String randomName = RandomStringUtils.randomAlphabetic(20);
            executor.update("insert into canal.canal_test(name) values('"+randomName+"')");
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        } finally {
            try {
                connector.disconnect();
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }
    }
}
