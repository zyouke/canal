package com.alibaba.otter.canal.server.netty4.handler;

import com.alibaba.otter.canal.common.zookeeper.running.ServerRunningMonitor;
import com.alibaba.otter.canal.common.zookeeper.running.ServerRunningMonitors;
import com.alibaba.otter.canal.protocol.CanalPacket;
import com.alibaba.otter.canal.protocol.ClientIdentity;
import com.alibaba.otter.canal.server.embedded.CanalServerWithEmbedded;
import com.alibaba.otter.canal.server.netty4.Netty4Utils;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import org.apache.commons.lang.StringUtils;
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
public class ClientAuthenticationHandlerNetty4 extends SimpleChannelInboundHandler<CanalPacket.ClientAuth> {

    private static final Logger logger = LoggerFactory.getLogger(ClientAuthenticationHandlerNetty4.class);
    private final int defaultSubscriptorDisconnectIdleTimeout = 5 * 60 * 1000;
    private CanalServerWithEmbedded embeddedServer;

    public ClientAuthenticationHandlerNetty4(CanalServerWithEmbedded embeddedServer) {
        this.embeddedServer = embeddedServer;
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, final CanalPacket.ClientAuth clientAuth) throws Exception{
        logger.info("进入身份认证的Handler [username {}]----[password {}] ",clientAuth.getUsername(),clientAuth.getPassword().toStringUtf8());
        Channel channel = ctx.channel();
        if(StringUtils.isNotEmpty(clientAuth.getDestination()) && StringUtils.isNotEmpty(clientAuth.getClientId())){
            ClientIdentity clientIdentity = new ClientIdentity(clientAuth.getDestination(), Short.valueOf(clientAuth.getClientId()), clientAuth.getFilter());
            try{
                MDC.put("destination", clientIdentity.getDestination());
                embeddedServer.subscribe(clientIdentity);
                channel.attr(AttributeKey.valueOf("clientIdentity")).set(clientIdentity);
                // 尝试启动，如果已经启动，忽略
                if(!embeddedServer.isStart(clientIdentity.getDestination())){
                    ServerRunningMonitor runningMonitor = ServerRunningMonitors.getRunningMonitor(clientIdentity.getDestination());
                    if(!runningMonitor.isStart()){
                        runningMonitor.start();
                    }
                }
            }finally{
                MDC.remove("destination");
            }
        }
        Netty4Utils.ack(channel,new ChannelFutureListener() {
                    public void operationComplete(ChannelFuture future) throws Exception{
                        logger.info("remove unused channel handlers after authentication is done successfully.");
                        ctx.pipeline().remove(HandshakeInitializationHandlerNetty4.class.getName());
                        ctx.pipeline().remove(ClientAuthenticationHandlerNetty4.class.getName());
                        int readTimeout = defaultSubscriptorDisconnectIdleTimeout;
                        int writeTimeout = defaultSubscriptorDisconnectIdleTimeout;
                        if(clientAuth.getNetReadTimeout() > 0){
                            readTimeout = clientAuth.getNetReadTimeout();
                        }
                        if(clientAuth.getNetWriteTimeout() > 0){
                            writeTimeout = clientAuth.getNetWriteTimeout();
                        }
                        IdleStateHandler idleStateHandler = new IdleStateHandler(readTimeout, writeTimeout, 0, TimeUnit.MILLISECONDS);
                        ctx.pipeline().addBefore(SessionHandlerNetty4.class.getName(), IdleStateHandler.class.getName(), idleStateHandler);
                    }

                });
    }
}
