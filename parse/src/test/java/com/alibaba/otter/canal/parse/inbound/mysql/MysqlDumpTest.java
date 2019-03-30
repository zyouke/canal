package com.alibaba.otter.canal.parse.inbound.mysql;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.BitSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.otter.canal.parse.inbound.SinkFunction;
import com.taobao.tddl.dbsync.binlog.LogEvent;
import com.taobao.tddl.dbsync.binlog.event.RowsLogEvent;
import org.junit.Assert;
import org.junit.Test;

import com.alibaba.otter.canal.filter.aviater.AviaterRegexFilter;
import com.alibaba.otter.canal.parse.exception.CanalParseException;
import com.alibaba.otter.canal.parse.index.AbstractLogPositionManager;
import com.alibaba.otter.canal.parse.stub.AbstractCanalEventSinkTest;
import com.alibaba.otter.canal.parse.support.AuthenticationInfo;
import com.alibaba.otter.canal.protocol.CanalEntry.Column;
import com.alibaba.otter.canal.protocol.CanalEntry.Entry;
import com.alibaba.otter.canal.protocol.CanalEntry.EntryType;
import com.alibaba.otter.canal.protocol.CanalEntry.EventType;
import com.alibaba.otter.canal.protocol.CanalEntry.RowChange;
import com.alibaba.otter.canal.protocol.CanalEntry.RowData;
import com.alibaba.otter.canal.protocol.position.EntryPosition;
import com.alibaba.otter.canal.protocol.position.LogPosition;
import com.alibaba.otter.canal.sink.exception.CanalSinkException;

public class MysqlDumpTest {
    @Test
    public void testSimple(){
        final MysqlEventParser controller = new MysqlEventParser();
        final EntryPosition startPosition = new EntryPosition();
        controller.setConnectionCharset(Charset.forName("UTF-8"));
        controller.setSlaveId(3344L);
        controller.setDetectingEnable(false);
        controller.setMasterInfo(new AuthenticationInfo(new InetSocketAddress("122.114.90.68", 3306), "canal", "123456"));
        controller.setMasterPosition(startPosition);
        controller.setEnableTsdb(false);
        controller.setDetectingSQL("show master status");
        controller.setDestination("zyouke");
        controller.setTsdbSpringXml("");
        controller.setEventFilter(new AviaterRegexFilter("zyouke\\..*"));
        controller.setEventBlackFilter(new AviaterRegexFilter(""));
        controller.setEventSink(new AbstractCanalEventSinkTest<List<Entry>>() {
            public boolean sink(List<Entry> entrys, InetSocketAddress remoteAddress, String destination) throws CanalSinkException, InterruptedException{
                for(Entry entry : entrys){
                    if(entry.getEntryType() == EntryType.TRANSACTIONBEGIN || entry.getEntryType() == EntryType.TRANSACTIONEND || entry.getEntryType() == EntryType.HEARTBEAT){
                        continue;
                    }
                    RowChange rowChage = null;
                    try{
                        rowChage = RowChange.parseFrom(entry.getStoreValue());
                    }catch(Exception e){
                        throw new RuntimeException("ERROR ## parser of eromanga-event has an error , data:" + entry.toString(), e);
                    }
                    EventType eventType = rowChage.getEventType();
                    System.out.println(String.format("================> binlog[%s:%s] , name[%s,%s] , eventType : %s",
                                                                        entry.getHeader().getLogfileName(), entry.getHeader().getLogfileOffset(),
                                                                        entry.getHeader().getSchemaName(), entry.getHeader().getTableName(), eventType));
                    if(eventType == EventType.QUERY || rowChage.getIsDdl()){
                        System.out.println(" sql ----> " + rowChage.getSql());
                    }
                    for(RowData rowData : rowChage.getRowDatasList()){
                        if(eventType == EventType.DELETE){
                            print(rowData.getBeforeColumnsList());
                        }else if(eventType == EventType.INSERT){
                            print(rowData.getAfterColumnsList());
                        }else{
                            System.out.println("-------> before");
                            print(rowData.getBeforeColumnsList());
                            System.out.println("-------> after");
                            print(rowData.getAfterColumnsList());
                        }
                    }
                }
                return true;
            }

        });
        controller.setLogPositionManager(new AbstractLogPositionManager() {
            @Override
            public LogPosition getLatestIndexBy(String destination){
                return null;
            }
            @Override
            public void persistLogPosition(String destination, LogPosition logPosition) throws CanalParseException{
                System.out.println(logPosition);
            }
        });
        controller.start();
        controller.buildHeartBeatTimeTask(controller.buildErosaConnection());
        try{
            Thread.sleep(100 * 1000 * 1000L);
        }catch(InterruptedException e){
            Assert.fail(e.getMessage());
        }
        controller.stop();
    }

    @Test
    public void dumpTest() throws IOException{
        MysqlConnection mysqlConnection = new MysqlConnection(new InetSocketAddress("122.114.90.68", 3306),"canal","123456");
        mysqlConnection.setSlaveId(1);
        mysqlConnection.connect();
        mysqlConnection.dump("mysql-bin.000015", 101557093L, new SinkFunction() {
            @Override
            public boolean sink(Object event){
                if(event instanceof RowsLogEvent){
                    RowsLogEvent rowsLogEvent = (RowsLogEvent) event;
                    System.out.println("--------------" + rowsLogEvent.getHeader().getType());
                    System.out.println("---------------" + rowsLogEvent.getTable().getTableName());
                }
                return true;
            }
        });
    }





    private void print(List<Column> columns){
        for(Column column : columns){
            System.out.println(column.getName() + " : " + column.getValue() + "    update=" + column.getUpdated());
        }
    }
}
