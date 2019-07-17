package com.zyouke.tomcat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * @author beifengtz
 * <a href='http://www.beifengtz.com'>www.beifengtz.com</a>
 * <p>location: mytomcat.javase_learning</p>
 * Created in 14:49 2019/4/21
 */
public class MyResponse {
    private SelectionKey selectionKey;

    public MyResponse(SelectionKey selectionKey){
        this.selectionKey = selectionKey;
    }

    public void write(String content) throws IOException{
        //  拼接相应数据包
        StringBuffer httpResponse = new StringBuffer();
        httpResponse.append("HTTP/1.1 200 OK\n")
                .append("Content-type:text/html\n")
                .append("\r\n")
                .append("<html><body>")
                .append(content)
                .append("</body></html>");

        // 转换为ByteBuffer
        ByteBuffer bb = ByteBuffer.wrap(httpResponse.toString().getBytes(StandardCharsets.UTF_8));
        SocketChannel channel = (SocketChannel) selectionKey.channel(); //  从契约获取通道
        long len = channel.write(bb);   //  向通道中写入数据
        if (len == -1){
            selectionKey.cancel();
        }
        bb.flip();
        channel.close();
        selectionKey.cancel();
    }
}
