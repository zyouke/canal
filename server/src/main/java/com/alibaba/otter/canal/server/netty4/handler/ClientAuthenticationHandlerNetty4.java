package com.alibaba.otter.canal.server.netty4.handler;

import com.alibaba.otter.canal.common.zookeeper.running.ServerRunningMonitor;
import com.alibaba.otter.canal.common.zookeeper.running.ServerRunningMonitors;
import com.alibaba.otter.canal.protocol.CanalPacket;
import com.alibaba.otter.canal.protocol.ClientIdentity;
import com.alibaba.otter.canal.server.embedded.CanalServerWithEmbedded;
import com.alibaba.otter.canal.server.netty.NettyUtils;
import com.alibaba.otter.canal.server.netty.handler.ClientAuthenticationHandler;
import com.alibaba.otter.canal.server.netty.handler.HandshakeInitializationHandler;
import com.alibaba.otter.canal.server.netty.handler.SessionHandler;
import com.alibaba.otter.canal.server.netty4.Netty4Utils;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.commons.lang3.StringUtils;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.concurrent.TimeUnit;

/**
 * 客户端发送身份认证请求的处理器
 *
 * @author jianghang 2012-10-24 上午11:12:45
 * @version 1.0.0
 */
public class ClientAuthenticationHandlerNetty4 extends SimpleChannelInboundHandler<CanalPacket.Packet> {

    private static final Logger logger = LoggerFactory.getLogger(ClientAuthenticationHandlerNetty4.class);
    private final long defaultSubscriptorDisconnectIdleTimeout = 5 * 60 * 1000;
    private CanalServerWithEmbedded embeddedServer;
    private final int SUPPORTED_VERSION = 3;

    public ClientAuthenticationHandlerNetty4(CanalServerWithEmbedded embeddedServer) {
        this.embeddedServer = embeddedServer;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, CanalPacket.Packet packet) throws Exception {
        switch (packet.getVersion()) {
            case SUPPORTED_VERSION:
            default:
                final CanalPacket.ClientAuth clientAuth = CanalPacket.ClientAuth.parseFrom(packet.getBody());
                logger.info("进入身份认证的Handler [username {}]----[password {}] ",clientAuth.getUsername(),clientAuth.getPassword().toStringUtf8());
                // 如果存在订阅信息
                if (StringUtils.isNotEmpty(clientAuth.getDestination()) && StringUtils.isNotEmpty(clientAuth.getClientId())) {
                    ClientIdentity clientIdentity = new ClientIdentity(clientAuth.getDestination(),Short.valueOf(clientAuth.getClientId()),clientAuth.getFilter());
                    try {
                        MDC.put("destination", clientIdentity.getDestination());
                        embeddedServer.subscribe(clientIdentity);
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
                Netty4Utils.ack(ctx.channel(), new ChannelFutureListener() {
                    public void operationComplete(ChannelFuture future) throws Exception {
                        logger.info("remove unused channel handlers after authentication is done successfully.");
                        ctx.pipeline().remove(HandshakeInitializationHandlerNetty4.class.getName());
                        ctx.pipeline().remove(ClientAuthenticationHandlerNetty4.class.getName());

                        long readTimeout = defaultSubscriptorDisconnectIdleTimeout;
                        long writeTimeout = defaultSubscriptorDisconnectIdleTimeout;
                        if (clientAuth.getNetReadTimeout() > 0) {
                            readTimeout = clientAuth.getNetReadTimeout();
                        }
                        if (clientAuth.getNetWriteTimeout() > 0) {
                            writeTimeout = clientAuth.getNetWriteTimeout();
                        }
                        IdleStateHandler idleStateHandler = new IdleStateHandler(readTimeout, writeTimeout, 0, TimeUnit.MILLISECONDS);
                        ctx.pipeline().addBefore(SessionHandlerNetty4.class.getName(),
                                                 IdleStateHandler.class.getName(),idleStateHandler);
                    }

                });
                break;
        }
    }
}
