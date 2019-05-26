package com.alibaba.otter.canal.server.netty4;

import com.alibaba.otter.canal.common.AbstractCanalLifeCycle;
import com.alibaba.otter.canal.server.CanalServer;
import com.alibaba.otter.canal.server.embedded.CanalServerWithEmbedded;
import com.alibaba.otter.canal.server.netty4.handler.ClientAuthenticationHandlerNetty4;
import com.alibaba.otter.canal.server.netty4.handler.HandshakeInitializationHandlerNetty4;
import com.alibaba.otter.canal.server.netty4.handler.SessionHandlerNetty4;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * 使用netty4开发服务端
 */
public class CanalServerWithNetty4 extends AbstractCanalLifeCycle implements CanalServer{
    private static final Logger LOGGER = LoggerFactory.getLogger(CanalServerWithNetty4.class);
    private CanalServerWithEmbedded embeddedServer;      // 嵌入式server
    private String ip;
    private int port;
    private Channel serverChannel = null;
    private ServerBootstrap serverBootstrap = null;
    private ChannelGroup childGroups = null; // socket channel
    private static class SingletonHolder {

        private static final CanalServerWithNetty4 CANAL_SERVER_WITH_NETTY4 = new CanalServerWithNetty4();
    }

    private CanalServerWithNetty4(){
        this.embeddedServer = CanalServerWithEmbedded.instance();
    }

    public static CanalServerWithNetty4 instance(){
        return SingletonHolder.CANAL_SERVER_WITH_NETTY4;
    }

    public void start(){
        LOGGER.info("。。。。。。。启动netty服务byCanalServerWithNetty4。。。。。。。。。");
        super.start();

        if(!embeddedServer.isStart()){
            embeddedServer.start();
        }
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();
        serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(boss,worker);
        serverBootstrap.channel(NioServerSocketChannel.class).
        childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch){
                ChannelPipeline pipelines = ch.pipeline();
                pipelines.addLast(HandshakeInitializationHandlerNetty4.class.getName(), new HandshakeInitializationHandlerNetty4());
                pipelines.addLast(ClientAuthenticationHandlerNetty4.class.getName(), new ClientAuthenticationHandlerNetty4(embeddedServer));
                SessionHandlerNetty4 sessionHandler = new SessionHandlerNetty4(embeddedServer);
                pipelines.addLast(SessionHandlerNetty4.class.getName(), sessionHandler);
            }
        });
        // 启动
        try{
            if(StringUtils.isNotEmpty(ip)){
                this.serverChannel = serverBootstrap.bind(new InetSocketAddress(this.ip, this.port)).sync().channel();
            }else{
                this.serverChannel = serverBootstrap.bind(new InetSocketAddress(this.port)).sync().channel();
            }
            // this.serverChannel.closeFuture() 方法会返回ChannelFuture，异步执行的，main方法会直接结束，所以sync() 方法必须要调用来阻塞程
            this.serverChannel.closeFuture().sync();
        }catch(InterruptedException e){
            LOGGER.error(e.getMessage(),e);
        }finally{
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    public void stop(){
        super.stop();

        if(this.serverChannel != null){
            this.serverChannel.close().awaitUninterruptibly(1000);
        }

        if(this.childGroups != null){
            this.childGroups.close().awaitUninterruptibly(5000);
        }

        if(this.serverBootstrap != null){
            //this.bootstrap.releaseExternalResources();
        }

        if(embeddedServer.isStart()){
            embeddedServer.stop();
        }
    }

    public void setIp(String ip){
        this.ip = ip;
    }

    public void setPort(int port){
        this.port = port;
    }

    public void setEmbeddedServer(CanalServerWithEmbedded embeddedServer){
        this.embeddedServer = embeddedServer;
    }





}
