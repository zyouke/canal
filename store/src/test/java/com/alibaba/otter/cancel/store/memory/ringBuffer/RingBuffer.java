package com.alibaba.otter.cancel.store.memory.ringBuffer;

import com.alibaba.otter.canal.common.utils.JsonUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * canal Memory内存的RingBuffer设计
 * 对RingBuffer的实现测试类
 */
public class RingBuffer {
    private int ringBufferSize = 8;
    private String[] ringBufferArr = new String[ringBufferSize];
    private AtomicInteger putSequence = new AtomicInteger(-1); // 代表当前put操作最后一次写操作发生的位置
    private AtomicInteger getSequence = new AtomicInteger(-1); // 代表当前get操作读取的最后一条的位置

    public boolean put(String str){
        if(full()){
            System.out.println("阻塞中,获取当前put和get的位置 ：【" +putSequence.get()+" : "+getSequence.get()+"】");
            return false;
        }
        int index = getIndex();
        ringBufferArr[index] = str;
        return true;
    }
    public String get(){
        int getIndex = (getSequence.get() + 1) % ringBufferSize;
        String str = null;
        do{
            try{
                Thread.sleep(100);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
            str = ringBufferArr[getIndex];
        }while(StringUtils.isBlank(str));
        getSequence.getAndIncrement();
        System.out.println(Thread.currentThread().getName() + "获取的数据 【" + getIndex +"----------"+ str + "】");
        return str;
    }
    private boolean full(){
        // 如果为第一次添加数据,不判断满
        if(putSequence.get() == -1){
            return false;
        }
        return (putSequence.get() + 1) % ringBufferSize == getSequence.get() + 1;
    }
    private int getIndex(){
        return putSequence.incrementAndGet() % ringBufferSize;
    }

    @Test
    public void putAndGetSimpleTest(){
        RingBuffer ringBuffer = new RingBuffer();
        for(int i = 0; i < ringBufferSize + 2; i++){
            ringBuffer.put(UUID.randomUUID().toString());
        }
        for(int i = 0; i < ringBufferSize; i++){
            ringBuffer.get();
        }
    }


    @Test
    public void putAndGetMayThreadTest(){
        final RingBuffer ringBuffer = new RingBuffer();
        Thread threadPut = new Thread(new Runnable() {
            @Override
            public void run(){
                while(true){
                    boolean putBoo = ringBuffer.put(UUID.randomUUID().toString());
                    if(putBoo){
                        System.out.println(Thread.currentThread().getName() + "添加数据之后 : " + JsonUtils.marshalToString(ringBuffer.ringBufferArr));
                    }
                    try{
                        Thread.sleep(50);
                    }catch(InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        });
        threadPut.start();
        Thread threadGet = new Thread(new Runnable() {
            @Override
            public void run(){
                while(true){
                    ringBuffer.get();
                }
            }
        });
        threadGet.start();
        try{
            Thread.sleep(5000);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }



}
