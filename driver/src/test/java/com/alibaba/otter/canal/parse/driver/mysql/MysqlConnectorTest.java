package com.alibaba.otter.canal.parse.driver.mysql;

import com.alibaba.otter.canal.parse.driver.mysql.packets.server.OKPacket;
import com.alibaba.otter.canal.parse.driver.mysql.packets.server.ResultSetPacket;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class MysqlConnectorTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(MysqlConnectorTest.class);
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
            ExecutorService executorService = Executors.newCachedThreadPool();
            for (int i = 0; i < 1000; i++) {
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ResultSetPacket result =  executor.query("select * from canal.canal where id = 51509");
                            System.out.println(result);
                        } catch (IOException e) {

                        }
                    }
                });
            }
            executorService.shutdown();
            while (!executorService.isTerminated());
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
    public void insertTest() throws Exception{
        MysqlUpdateExecutor executor = new MysqlUpdateExecutor(connector);
        for (int i = 0; i < 1; i++) {
            String randomName = RandomStringUtils.randomAlphabetic(20);
            executor.update("insert into canal.canal(name) values('"+randomName+"')");
        }
    }



    @Test
    public void manyThreadInsertTest() throws Exception{
        MysqlUpdateExecutor executor = new MysqlUpdateExecutor(connector);
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < 10; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        String randomName = RandomStringUtils.randomAlphabetic(20);
                        executor.update("insert into canal.canal(name) values('" + randomName + "')");
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(),e);
                    }
                }
            });
        }
        executorService.shutdown();
        while (!executorService.isTerminated());
    }
}
