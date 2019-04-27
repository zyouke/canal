package com.alibaba.otter.cancel.store.memory.buffer;

import java.net.InetSocketAddress;

import org.junit.Assert;

import com.alibaba.otter.canal.protocol.CanalEntry.Entry;
import com.alibaba.otter.canal.protocol.CanalEntry.Header;
import com.alibaba.otter.canal.protocol.position.LogIdentity;
import com.alibaba.otter.canal.store.model.Event;

public class MemoryEventStoreBase {

    private static final String MYSQL_ADDRESS = "127.0.0.1";
    private String binlogFile = " mysql-bin.000015";

    protected void sleep(Long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            Assert.fail();
        }
    }

    protected Event buildEvent() {
        Header.Builder headerBuilder = Header.newBuilder();
        headerBuilder.setLogfileName(binlogFile);
        headerBuilder.setLogfileOffset(1L);
        headerBuilder.setExecuteTime(System.currentTimeMillis());
        headerBuilder.setEventLength(1024);
        headerBuilder.setServerId(1234L);
        Entry.Builder entryBuilder = Entry.newBuilder();
        entryBuilder.setHeader(headerBuilder.build());
        Entry entry = entryBuilder.build();

        return new Event(new LogIdentity(new InetSocketAddress(MYSQL_ADDRESS, 3306), 1234L), entry);
    }
}
