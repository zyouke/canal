package com.zyouke.canal;

import com.alibaba.otter.canal.common.AbstractCanalLifeCycle;
import com.alibaba.otter.canal.server.CanalServer;
import com.alibaba.otter.canal.server.embedded.CanalServerWithEmbedded;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.lang.StringUtils;

/**
 * 使用netty4开发服务端
 */
public class CanalServerWithNetty4 extends AbstractCanalLifeCycle implements CanalServer{



    private CanalServerWithEmbedded embeddedServer;      // 嵌入式server
    private String ip;
    private int port;
    private Channel serverChannel = null;
    private ServerBootstrap bootstrap = null;
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
        super.start();

        if(!embeddedServer.isStart()){
            embeddedServer.start();
        }
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(boss,worker);
        serverBootstrap.channel(NioServerSocketChannel.class).
        childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch){/*
                ChannelPipeline pipelines = ch.pipeline();
                pipelines.addLast(HandshakeInitializationHandler.class.getName(), new HandshakeInitializationHandler(childGroups));
                pipelines.addLast(ClientAuthenticationHandler.class.getName(), new ClientAuthenticationHandler(embeddedServer));
                SessionHandler sessionHandler = new SessionHandler(embeddedServer);
                pipelines.addLast(SessionHandler.class.getName(), sessionHandler);
            */}
        });
        // 启动
        if(StringUtils.isNotEmpty(ip)){
            //this.serverChannel = bootstrap.bind(new InetSocketAddress(this.ip, this.port));
        }else{
            //this.serverChannel = bootstrap.bind(new InetSocketAddress(this.port));
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

        if(this.bootstrap != null){
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
