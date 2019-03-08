package com.alibaba.otter.canal.server.netty.handler;

import com.alibaba.otter.canal.common.zookeeper.running.ServerRunningMonitor;
import com.alibaba.otter.canal.common.zookeeper.running.ServerRunningMonitors;
import com.alibaba.otter.canal.protocol.CanalPacket.ClientAuth;
import com.alibaba.otter.canal.protocol.CanalPacket.Packet;
import com.alibaba.otter.canal.protocol.ClientIdentity;
import com.alibaba.otter.canal.server.embedded.CanalServerWithEmbedded;
import com.alibaba.otter.canal.server.netty.NettyUtils;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.concurrent.TimeUnit;

/**
 * 客户端身份认证处理
 * 
 * @author jianghang 2012-10-24 上午11:12:45
 * @version 1.0.0
 */
public class ClientAuthenticationHandler extends SimpleChannelInboundHandler<Packet> {

    private static final Logger     logger                                  = LoggerFactory.getLogger(ClientAuthenticationHandler.class);
    private final int               SUPPORTED_VERSION                       = 3;
    private final int               defaultSubscriptorDisconnectIdleTimeout = 5 * 60 * 1000;
    private CanalServerWithEmbedded embeddedServer;

    public ClientAuthenticationHandler(){

    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, Packet packet) throws Exception {

        switch (packet.getVersion()) {
            case SUPPORTED_VERSION:
            default:
                final ClientAuth clientAuth = ClientAuth.parseFrom(packet.getBody());
                // 如果存在订阅信息
                if (StringUtils.isNotEmpty(clientAuth.getDestination())
                        && StringUtils.isNotEmpty(clientAuth.getClientId())) {
                    ClientIdentity clientIdentity = new ClientIdentity(clientAuth.getDestination(),
                            Short.valueOf(clientAuth.getClientId()),
                            clientAuth.getFilter());
                    try {
                        MDC.put("destination", clientIdentity.getDestination());
                        embeddedServer.subscribe(clientIdentity);
                        //ctx.setAttachment(clientIdentity);// 设置状态数据
                        // 尝试启动，如果已经启动，忽略
                        if (!embeddedServer.isStart(clientIdentity.getDestination())) {
                            ServerRunningMonitor runningMonitor = ServerRunningMonitors.getRunningMonitor(clientIdentity.getDestination());
                            if (!runningMonitor.isStart()) {
                                runningMonitor.start();
                            }
                        }
                    } finally {
                        MDC.remove("destination");
                    }
                }

                NettyUtils.ack(ctx.channel(), new ChannelFutureListener() {

                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        logger.info("remove unused channel handlers after authentication is done successfully.");
                        ctx.pipeline().remove(HandshakeInitializationHandler.class.getName());
                        ctx.pipeline().remove(ClientAuthenticationHandler.class.getName());

                        int readTimeout = defaultSubscriptorDisconnectIdleTimeout;
                        int writeTimeout = defaultSubscriptorDisconnectIdleTimeout;
                        if (clientAuth.getNetReadTimeout() > 0) {
                            readTimeout = clientAuth.getNetReadTimeout();
                        }
                        if (clientAuth.getNetWriteTimeout() > 0) {
                            writeTimeout = clientAuth.getNetWriteTimeout();
                        }
                        // fix bug: soTimeout parameter's unit from connector is
                        // millseconds.
                        IdleStateHandler idleStateHandler = new IdleStateHandler(readTimeout,writeTimeout,0,TimeUnit.MILLISECONDS);
                        ctx.pipeline().addBefore(SessionHandler.class.getName(),
                                IdleStateHandler.class.getName(),
                                idleStateHandler);
                        /*IdleStateAwareChannelHandler idleStateAwareChannelHandler = new IdleStateAwareChannelHandler() {

                            public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) throws Exception {
                                logger.warn("channel:{} idle timeout exceeds, close channel to save server resources...",
                                        ctx.channel());
                                ctx.channel().close();
                            }

                        };
                        ctx.pipeline().addBefore(SessionHandler.class.getName(),
                                IdleStateAwareChannelHandler.class.getName(),
                                idleStateAwareChannelHandler);*/
                    }

                });
                break;
        }

    }

    public ClientAuthenticationHandler(CanalServerWithEmbedded embeddedServer){
        this.embeddedServer = embeddedServer;
    }

    public void setEmbeddedServer(CanalServerWithEmbedded embeddedServer) {
        this.embeddedServer = embeddedServer;
    }

}
