package com.zyouke.replayingdecoder.client;

import com.zyouke.replayingdecoder.protocol.UserProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.Charset;

public class CodecClientHandler extends SimpleChannelInboundHandler<UserProtocol> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        for (int i = 0; i < 3; ++i) {
            String messageToBeSent = "sent from client ";
            byte[] content = messageToBeSent.getBytes(Charset.forName("utf-8"));
            int length = messageToBeSent.getBytes(Charset.forName("utf-8")).length;
            UserProtocol userProtocol = new UserProtocol();
            userProtocol.setLength(length);
            userProtocol.setContent(content);
            ctx.writeAndFlush(userProtocol);
        }
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, UserProtocol msg) throws Exception{
        int length = msg.getLength();
        byte[] content = msg.getContent();
        System.out.println("客户端接收到的内容：" + new String(content, Charset.forName("utf-8")));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}
