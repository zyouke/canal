package com.zyouke.tomcat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;

/**
 * @author beifengtz
 * <a href='http://www.beifengtz.com'>www.beifengtz.com</a>
 * <p>location: mytomcat.javase_learning</p>
 * Created in 14:45 2019/4/21
 */
public class MyRequest {
    private String url;
    private String method;
    private HashMap<String,String> param = new HashMap<>();

    public MyRequest(SelectionKey selectionKey) throws IOException{
        //  从契约获取通道
        SocketChannel channel = (SocketChannel) selectionKey.channel();

        String httpRequest = "";
        ByteBuffer bb = ByteBuffer.allocate(16*1024);   //  从堆内存中获取内存
        int length = 0; //  读取byte数组的长度
        length = channel.read(bb);  //  从通道中读取数据到ByteBuffer容器中
        if (length < 0){
            selectionKey.cancel();  //  取消该契约
        }else {
            httpRequest = new String(bb.array()).trim();    //  将ByteBuffer转为String
            String httpHead = httpRequest.split("\n")[0];   //  获取请求头
            url = httpHead.split("\\s")[1].split("\\?")[0]; //  获取请求路径
            String path = httpHead.split("\\s")[1]; //  请求全路径，包含get的参数数据
            method = httpHead.split("\\s")[0];

            //  一下是拆分get请求的参数数据
            String[] params = path.indexOf("?") > 0 ? path.split("\\?")[1].split("\\&") : null;
            if (params != null){
                try{
                    for (String tmp : params){
                        param.put(tmp.split("\\=")[0],tmp.split("\\=")[1]);
                    }
                }catch (NullPointerException e){
                    e.printStackTrace();
                }
            }
            System.out.println(this);
        }
        bb.flip();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public String toString() {
        return "MyRequest{" +
                "url='" + url + '\'' +
                ", method='" + method + '\'' +
                ", param=" + param +
                '}';
    }
}
