package com.alibaba.otter.cancel.store.memory.buffer;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.alibaba.otter.canal.protocol.position.Position;
import com.alibaba.otter.canal.store.helper.CanalEventUtils;
import com.alibaba.otter.canal.store.memory.MemoryEventStoreWithBuffer;
import com.alibaba.otter.canal.store.model.Event;
import com.alibaba.otter.canal.store.model.Events;

/**
 * 测试下rollback / ack的操作
 * 
 * @author jianghang 2012-6-19 下午09:49:28
 * @version 1.0.0
 */
public class MemoryEventStoreRollbackAndAckTest extends MemoryEventStoreBase {

    @Test
    public void testRollback() {
        int bufferSize = 8;
        MemoryEventStoreWithBuffer eventStore = new MemoryEventStoreWithBuffer();
        eventStore.setBufferSize(bufferSize);
        eventStore.start();
        int forCount = bufferSize/2;
        for (int i = 0; i < forCount ; i++) {
            boolean result = eventStore.tryPut(buildEvent());
            sleep(100L);
            Assert.assertTrue(result);
        }
        sleep(50L);
        Position first = eventStore.getFirstPosition();
        Position lastest = eventStore.getLatestPosition();
        Assert.assertEquals(first, CanalEventUtils.createPosition(buildEvent()));
        Assert.assertEquals(lastest, CanalEventUtils.createPosition(buildEvent()));

        System.out.println("start get");
        Events<Event> entrys1 = eventStore.tryGet(first, bufferSize);
        System.out.println("first get size : " + entrys1.getEvents().size());
        eventStore.rollback();
        entrys1 = eventStore.tryGet(first, bufferSize);
        System.out.println("after rollback get size : " + entrys1.getEvents().size());
        Assert.assertTrue(entrys1.getEvents().size() == forCount );
        // 继续造数据
        int forStart = forCount;
        forCount = bufferSize ;
        for (int i = forStart ; i < forCount; i++) {
            boolean result = eventStore.tryPut(buildEvent());
            sleep(100L);
            Assert.assertTrue(result);
        }
        Events<Event> entrys2 = eventStore.tryGet(entrys1.getPositionRange().getEnd(), bufferSize);
        System.out.println("second get size : " + entrys2.getEvents().size());
        eventStore.rollback();
        entrys2 = eventStore.tryGet(entrys1.getPositionRange().getEnd(), bufferSize);
        System.out.println("after rollback get size : " + entrys2.getEvents().size());
        Assert.assertTrue(entrys2.getEvents().size() == bufferSize);
        first = eventStore.getFirstPosition();
        lastest = eventStore.getLatestPosition();
        List<Event> entrys = new ArrayList<Event>(entrys2.getEvents());
        Assert.assertTrue(entrys.size() == bufferSize);
        Assert.assertEquals(first, entrys2.getPositionRange().getStart());
        Assert.assertEquals(lastest, entrys2.getPositionRange().getEnd());
        Assert.assertEquals(first, CanalEventUtils.createPosition(entrys.get(0)));
        Assert.assertEquals(lastest, CanalEventUtils.createPosition(entrys.get(bufferSize - 1)));
        eventStore.stop();
    }

    @Test
    public void testAck() {
        int bufferSize = 16;
        MemoryEventStoreWithBuffer eventStore = new MemoryEventStoreWithBuffer();
        eventStore.setBufferSize(bufferSize);
        eventStore.start();

        for (int i = 0; i < bufferSize ; i++) {
            boolean result = eventStore.tryPut(buildEvent());
            sleep(100L);
            Assert.assertTrue(result);
        }

        sleep(50L);
        Position first = eventStore.getFirstPosition();
        Position lastest = eventStore.getLatestPosition();
        Assert.assertEquals(first, CanalEventUtils.createPosition(buildEvent()));
        Assert.assertEquals(lastest, CanalEventUtils.createPosition(buildEvent()));

        System.out.println("start get");
        Events<Event> entrys1 = eventStore.tryGet(first, bufferSize);
        System.out.println("first get size : " + entrys1.getEvents().size());

        eventStore.cleanUntil(entrys1.getPositionRange().getEnd());
        sleep(50L);

        // 继续造数据
        for (int i = bufferSize ; i < bufferSize; i++) {
            boolean result = eventStore.tryPut(buildEvent());
            sleep(100L);
            Assert.assertTrue(result);
        }

        Events<Event> entrys2 = eventStore.tryGet(entrys1.getPositionRange().getEnd(), bufferSize);
        System.out.println("second get size : " + entrys2.getEvents().size());

        eventStore.rollback();

        entrys2 = eventStore.tryGet(entrys1.getPositionRange().getEnd(), bufferSize);
        System.out.println("after rollback get size : " + entrys2.getEvents().size());

        first = eventStore.getFirstPosition();
        lastest = eventStore.getLatestPosition();
        List<Event> entrys = new ArrayList<Event>(entrys2.getEvents());
        Assert.assertEquals(first, entrys2.getPositionRange().getStart());
        Assert.assertEquals(lastest, entrys2.getPositionRange().getEnd());

        Assert.assertEquals(first, CanalEventUtils.createPosition(entrys.get(0)));
        Assert.assertEquals(lastest, CanalEventUtils.createPosition(entrys.get(entrys.size() - 1)));

        // 全部ack掉
        eventStore.cleanUntil(entrys2.getPositionRange().getEnd());

        // 最后就拿不到数据
        Events<Event> entrys3 = eventStore.tryGet(entrys1.getPositionRange().getEnd(), bufferSize);
        System.out.println("third get size : " + entrys3.getEvents().size());
        Assert.assertEquals(0, entrys3.getEvents().size());

        eventStore.stop();
    }

}
