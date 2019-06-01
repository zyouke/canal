package com.alibaba.otter.canal.server.netty4.handler;

import com.alibaba.otter.canal.common.zookeeper.running.ServerRunningMonitor;
import com.alibaba.otter.canal.common.zookeeper.running.ServerRunningMonitors;
import com.alibaba.otter.canal.protocol.CanalEntry.Entry;
import com.alibaba.otter.canal.protocol.CanalPacket;
import com.alibaba.otter.canal.protocol.CanalPacket.*;
import com.alibaba.otter.canal.protocol.ClientIdentity;
import com.alibaba.otter.canal.protocol.Message;
import com.alibaba.otter.canal.server.embedded.CanalServerWithEmbedded;
import com.alibaba.otter.canal.server.netty4.Netty4Utils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.helpers.MessageFormatter;

import java.util.List;
import java.util.concurrent.TimeUnit;

/*
 * 处理具体的客户端请求
 *
 * @author jianghang 2012-10-24 下午02:21:13
 * @version 1.0.0
*/

public class SessionHandlerNetty4 extends SimpleChannelInboundHandler<CanalPacket.Packet>{

    private static final Logger logger = LoggerFactory.getLogger(SessionHandlerNetty4.class);
    private CanalServerWithEmbedded embeddedServer;

    public SessionHandlerNetty4(CanalServerWithEmbedded embeddedServer){
        this.embeddedServer = embeddedServer;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CanalPacket.Packet packet) throws Exception{
        Channel channel = ctx.channel();
        logger.info("message receives in session handler...");
        ClientIdentity clientIdentity = null;
        try{
            switch(packet.getType()){
                case SUBSCRIPTION:
                    Sub sub = Sub.parseFrom(packet.getBody());
                    if(StringUtils.isNotEmpty(sub.getDestination()) && StringUtils.isNotEmpty(sub.getClientId())){
                        clientIdentity = new ClientIdentity(sub.getDestination(), Short.valueOf(sub.getClientId()), sub.getFilter());
                        MDC.put("destination", clientIdentity.getDestination());
                        // 尝试启动，如果已经启动，忽略
                        if(!embeddedServer.isStart(clientIdentity.getDestination())){
                            ServerRunningMonitor runningMonitor = ServerRunningMonitors.getRunningMonitor(clientIdentity.getDestination());
                            if(!runningMonitor.isStart()){
                                runningMonitor.start();
                            }
                        }

                        embeddedServer.subscribe(clientIdentity);
                        channel.attr(AttributeKey.valueOf("clientIdentity")).set(clientIdentity);// 设置状态数据
                        Netty4Utils.ack(ctx.channel(), null);
                    }else{
                        Netty4Utils.error(401, MessageFormatter.format("destination or clientId is null", sub.toString()).getMessage(), channel, null);
                    }
                    break;
                case UNSUBSCRIPTION:
                    Unsub unsub = Unsub.parseFrom(packet.getBody());
                    if(StringUtils.isNotEmpty(unsub.getDestination()) && StringUtils.isNotEmpty(unsub.getClientId())){
                        clientIdentity = new ClientIdentity(unsub.getDestination(), Short.valueOf(unsub.getClientId()), unsub.getFilter());
                        MDC.put("destination", clientIdentity.getDestination());
                        embeddedServer.unsubscribe(clientIdentity);
                        stopCanalInstanceIfNecessary(clientIdentity);// 尝试关闭
                        Netty4Utils.ack(ctx.channel(), null);
                    }else{
                        Netty4Utils.error(401, MessageFormatter.format("destination or clientId is null", unsub.toString()).getMessage(), channel, null);
                    }
                    break;
                case GET:
                    Get get = Get.parseFrom(packet.getBody());
                    if(StringUtils.isNotEmpty(get.getDestination()) && StringUtils.isNotEmpty(get.getClientId())){
                        clientIdentity = new ClientIdentity(get.getDestination(), Short.valueOf(get.getClientId()));
                        MDC.put("destination", clientIdentity.getDestination());
                        Message message = null;
                        if(get.getTimeout() == -1){// 是否是初始值
                            message = embeddedServer.getWithoutAck(clientIdentity, get.getFetchSize());
                        }else{
                            TimeUnit unit = convertTimeUnit(get.getUnit());
                            message = embeddedServer.getWithoutAck(clientIdentity, get.getFetchSize(), get.getTimeout(), unit);
                        }
                        Packet.Builder packetBuilder = Packet.newBuilder();
                        packetBuilder.setType(PacketType.MESSAGES);

                        Messages.Builder messageBuilder = Messages.newBuilder();
                        messageBuilder.setBatchId(message.getId());
                        List<Entry> entries = message.getEntries();
                        if(message.getId() != -1 && entries != null && entries.size() > 0){
                            for(Entry entry : entries){
                                messageBuilder.addMessages(entry.toByteString());
                            }
                        }
                        packetBuilder.setBody(messageBuilder.build().toByteString());
                        Netty4Utils.write(ctx.channel(), packetBuilder.build().toByteArray(), null);// 输出数据
                    }else{
                        Netty4Utils.error(401,
                                MessageFormatter.format("destination or clientId is null", get.toString()).getMessage(), channel, null);
                    }
                    break;
                case CLIENTACK:
                    ClientAck ack = ClientAck.parseFrom(packet.getBody());
                    MDC.put("destination", ack.getDestination());
                    if(StringUtils.isNotEmpty(ack.getDestination()) && StringUtils.isNotEmpty(ack.getClientId())){
                        if(ack.getBatchId() == 0L){
                            Netty4Utils.error(402,
                                    MessageFormatter.format("batchId should assign value", ack.toString()).getMessage(), channel, null);
                        }else if(ack.getBatchId() == -1L){ // -1代表上一次get没有数据，直接忽略之
                           logger.info("上一次get没有数据，直接忽略之,打印日志。。。。。");
                        }else{
                            clientIdentity = new ClientIdentity(ack.getDestination(), Short.valueOf(ack.getClientId()));
                            embeddedServer.ack(clientIdentity, ack.getBatchId());
                        }
                    }else{
                        Netty4Utils.error(401,
                                MessageFormatter.format("destination or clientId is null", ack.toString()).getMessage(), channel, null);
                    }
                    break;
                case CLIENTROLLBACK:
                    ClientRollback rollback = ClientRollback.parseFrom(packet.getBody());
                    MDC.put("destination", rollback.getDestination());
                    if(StringUtils.isNotEmpty(rollback.getDestination()) && StringUtils.isNotEmpty(rollback.getClientId())){
                        clientIdentity = new ClientIdentity(rollback.getDestination(), Short.valueOf(rollback.getClientId()));
                        if(rollback.getBatchId() == 0L){
                            embeddedServer.rollback(clientIdentity);// 回滚所有批次
                        }else{
                            embeddedServer.rollback(clientIdentity, rollback.getBatchId()); // 只回滚单个批次
                        }
                    }else{
                        Netty4Utils.error(401,
                                MessageFormatter.format("destination or clientId is null", rollback.toString()).getMessage(), channel, null);
                    }
                    break;
                default:
                    Netty4Utils.error(400,
                            MessageFormatter.format("packet type={} is NOT supported!", packet.getType()).getMessage(), channel, null);
                    break;
            }
        }catch(Throwable exception){
            Netty4Utils.error(400,
                    MessageFormatter.format("something goes wrong with channel:{}, exception={}", channel, ExceptionUtils.getStackTrace(exception)).getMessage(), channel, null);
        }finally{
            MDC.remove("destination");
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception{
        logger.error("something goes wrong with channel:{}, exception={}", ctx.channel(), ExceptionUtils.getStackTrace(cause.getCause()));
        ctx.channel().close();
    }


    private void stopCanalInstanceIfNecessary(ClientIdentity clientIdentity){
        List<ClientIdentity> clientIdentitys = embeddedServer.listAllSubscribe(clientIdentity.getDestination());
        if(clientIdentitys != null && clientIdentitys.size() == 1 && clientIdentitys.contains(clientIdentity)){
            ServerRunningMonitor runningMonitor = ServerRunningMonitors.getRunningMonitor(clientIdentity.getDestination());
            if(runningMonitor.isStart()){
                runningMonitor.release();
            }
        }
    }

    private TimeUnit convertTimeUnit(int unit){
        switch(unit){
            case 0:
                return TimeUnit.NANOSECONDS;
            case 1:
                return TimeUnit.MICROSECONDS;
            case 2:
                return TimeUnit.MILLISECONDS;
            case 3:
                return TimeUnit.SECONDS;
            case 4:
                return TimeUnit.MINUTES;
            case 5:
                return TimeUnit.HOURS;
            case 6:
                return TimeUnit.DAYS;
            default:
                return TimeUnit.MILLISECONDS;
        }
    }


}
