package com.zyouke.promise;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @Author: zhoujun
 */
public class NettyFuture4Promise {
    private static NioEventLoopGroup loop = null;
    static {
        loop = new NioEventLoopGroup(8);
    }
    public static void main(String[] args) throws Exception{
        NettyFuture4Promise nettyFuture4Promise = new NettyFuture4Promise();
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < 10; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Promise<String> promise = nettyFuture4Promise.search("Netty In Action");
                        String result = promise.get();
                        System.out.println("price is " + result);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        executorService.shutdown();
        while (!executorService.isTerminated());
    }
    private Promise<String> search(String prod) {
        // 创建一个DefaultPromise并返回
        DefaultPromise<String> promise = new DefaultPromise<String>(loop.next());
        loop.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println(String.format("	>>search price of %s from internet!",prod));
                    Thread.sleep(1000);
                    promise.setSuccess("$99.99");// 等待5S后设置future为成功，
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },0, TimeUnit.SECONDS);

        return promise;
    }
}
