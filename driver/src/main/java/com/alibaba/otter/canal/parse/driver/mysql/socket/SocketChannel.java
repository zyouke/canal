package com.alibaba.otter.canal.parse.driver.mysql.socket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 封装netty的通信channel和数据接收缓存，实现读、写、连接校验的功能。 2016-12-28
 * 
 * @author luoyaogui
 */
public class SocketChannel {

    private Channel channel = null;
    private Object  lock    = new Object();
    private  volatile ByteBuf cache   = PooledByteBufAllocator.DEFAULT.directBuffer(1024 * 1024); // 缓存大小
    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public void writeCache(ByteBuf buf) {
        synchronized (lock) {
            cache.discardReadBytes();// 回收内存
            cache.writeBytes(buf);
        }
    }

    public void writeChannel(byte[]... buf) throws IOException {
        if (channel != null && channel.isWritable()) {
            ByteBuf byteBuf = Unpooled.copiedBuffer(buf);
            channel.writeAndFlush(byteBuf);
        } else {
            throw new IOException("write  failed  !  please checking !");
        }
    }

    public byte[] read(int readSize) throws IOException {
        do {
            if (readSize > cache.readableBytes()) {
                if (null == channel) {
                    throw new java.nio.channels.ClosedByInterruptException();
                }
                synchronized (this) {
                    try {
                        wait(100);
                    } catch (InterruptedException e) {
                        throw new java.nio.channels.ClosedByInterruptException();
                    }
                }
            } else {
                byte[] back = new byte[readSize];
                synchronized (lock) {
                    cache.readBytes(back);
                }
                return back;
            }
        } while (true);
    }

    public boolean isConnected() {
        return channel != null ? true : false;
    }

    public SocketAddress getRemoteSocketAddress() {
        return channel != null ? channel.remoteAddress() : null;
    }

    public void close() {
        if (channel != null) {
            channel.close();
        }
        channel = null;
        cache.discardReadBytes();// 回收已占用的内存
        cache.release();// 释放整个内存
        cache = null;
    }
}
