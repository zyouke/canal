package com.zyouke.replayingdecoder.server;

import com.zyouke.replayingdecoder.protocol.UserProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.Charset;
import java.util.UUID;

public class CodecServerHandler extends SimpleChannelInboundHandler<UserProtocol> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, UserProtocol msg) throws Exception{
        int length = msg.getLength();
        byte[] content = msg.getContent();
        System.out.println("服务端接收到的消息：" + new String(content, Charset.forName("utf-8")));
        String responseMessage = UUID.randomUUID().toString();
        int responseLength = responseMessage.getBytes("utf-8").length;
        byte[] responseContent = responseMessage.getBytes("utf-8");
        UserProtocol userProtocol = new UserProtocol();
        userProtocol.setLength(responseLength);
        userProtocol.setContent(responseContent);
        ctx.writeAndFlush(userProtocol);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
