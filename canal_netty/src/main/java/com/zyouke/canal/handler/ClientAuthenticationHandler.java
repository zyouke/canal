package com.zyouke.canal.handler;

import com.alibaba.otter.canal.protocol.CanalPacket;
import com.alibaba.otter.canal.server.embedded.CanalServerWithEmbedded;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
/*import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.jboss.netty.handler.timeout.IdleStateHandler;*/
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 客户端身份认证处理
 *
 * @author jianghang 2012-10-24 上午11:12:45
 * @version 1.0.0
 */
public class ClientAuthenticationHandler extends SimpleChannelInboundHandler<CanalPacket.ClientAuth> {

    private static final Logger logger = LoggerFactory.getLogger(ClientAuthenticationHandler.class);
    private final int defaultSubscriptorDisconnectIdleTimeout = 5 * 60 * 1000;
    private CanalServerWithEmbedded embeddedServer;

    public ClientAuthenticationHandler(CanalServerWithEmbedded embeddedServer) {
        this.embeddedServer = embeddedServer;
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CanalPacket.ClientAuth clientAuth) throws Exception{/*
        // 如果存在订阅信息
        if(StringUtils.isNotEmpty(clientAuth.getDestination()) && StringUtils.isNotEmpty(clientAuth.getClientId())){
            ClientIdentity clientIdentity = new ClientIdentity(clientAuth.getDestination(), Short.valueOf(clientAuth.getClientId()), clientAuth.getFilter());
            try{
                MDC.put("destination", clientIdentity.getDestination());
                embeddedServer.subscribe(clientIdentity);
                ctx.channel().attr()
                ctx.setAtt(clientIdentity);// 设置状态数据
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
        NettyUtils.ack(ctx.getChannel(), new ChannelFutureListener() {

            public void operationComplete(ChannelFuture future) throws Exception{
                logger.info("remove unused channel handlers after authentication is done successfully.");
                ctx.getPipeline().remove(HandshakeInitializationHandler.class.getName());
                ctx.getPipeline().remove(ClientAuthenticationHandler.class.getName());

                int readTimeout = defaultSubscriptorDisconnectIdleTimeout;
                int writeTimeout = defaultSubscriptorDisconnectIdleTimeout;
                if(clientAuth.getNetReadTimeout() > 0){
                    readTimeout = clientAuth.getNetReadTimeout();
                }
                if(clientAuth.getNetWriteTimeout() > 0){
                    writeTimeout = clientAuth.getNetWriteTimeout();
                }
                // fix bug: soTimeout parameter's unit from connector is
                // millseconds.
                IdleStateHandler idleStateHandler = new IdleStateHandler(NettyUtils.hashedWheelTimer, readTimeout, writeTimeout, 0, TimeUnit.MILLISECONDS);
                ctx.getPipeline().addBefore(SessionHandler.class.getName(), IdleStateHandler.class.getName(), idleStateHandler);

                IdleStateAwareChannelHandler idleStateAwareChannelHandler = new IdleStateAwareChannelHandler() {

                    public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) throws Exception{
                        logger.warn("channel:{} idle timeout exceeds, close channel to save server resources...", ctx.getChannel());
                        ctx.getChannel().close();
                    }

                };
                ctx.getPipeline().addBefore(SessionHandler.class.getName(), IdleStateAwareChannelHandler.class.getName(), idleStateAwareChannelHandler);
            }

        });
    */}
}
