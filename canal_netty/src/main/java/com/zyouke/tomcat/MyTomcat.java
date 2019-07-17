package com.zyouke.tomcat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author beifengtz
 * <a href='http://www.beifengtz.com'>www.beifengtz.com</a>
 * <p>location: mytomcat.javase_learning</p>
 * Created in 15:03 2019/4/21
 */
public class MyTomcat {
    private int port = 8080;
    private Map<String, String> urlServletMap = new HashMap<>();

    private Selector selector;
    private ExecutorService es = Executors.newCachedThreadPool();

    public MyTomcat() {
    }

    public MyTomcat(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        //  初始化映射关系
        initServletMapping();

        // 启动Selector
        selector = SelectorProvider.provider().openSelector();
        // 启动Channel
        ServerSocketChannel ssc = ServerSocketChannel.open();
        // 配置非阻塞选择
        ssc.configureBlocking(false);

        // 监听端口
        InetSocketAddress isa = new InetSocketAddress(port);
        ssc.socket().bind(isa);

        // 将Channel绑定到Selector上，并选择准备模式为Accept，此处可能会失败，后续可再次开启
        SelectionKey acceptKey = ssc.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("MyTomcat is started...");

        ConcurrentLinkedQueue<MyRequest> requestList = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<MyResponse> responseList = new ConcurrentLinkedQueue<>();

        while (true) {
            selector.select();  //  等待Channel准备数据
            Set readyKeys = selector.selectedKeys();
            Iterator i = readyKeys.iterator();

            while (i.hasNext()) {
                SelectionKey sk = (SelectionKey) i.next();
                i.remove(); //  从集合中移除，防止重复处理

                if (sk.isAcceptable()) { //  如果键的接收状态未正常打开，再次尝试打开
                    doAccept(sk);
                } else if (sk.isValid() && sk.isReadable()) {  // 可读
                    requestList.add(getRequest(sk));
                    //  切换准备状态
                    sk.interestOps(SelectionKey.OP_WRITE);
                } else if (sk.isValid() && sk.isWritable()) { //  可写
                    responseList.add(getResponse(sk));
                    //  切换准备状态
                    sk.interestOps(SelectionKey.OP_READ);
                }

                //  等待一对请求和响应均准备好时处理
                if (!requestList.isEmpty() && !responseList.isEmpty()) {
                    dispatch(requestList.poll(), responseList.poll());
                }
            }
        }
    }

    /**
     * 如果没有正常开启接收模式
     * 尝试开启接收模式
     * @param selectionKey
     */
    private void doAccept(SelectionKey selectionKey) {
        ServerSocketChannel server = (ServerSocketChannel) selectionKey.channel();
        SocketChannel clientChannel;
        try {
            clientChannel = server.accept();
            clientChannel.configureBlocking(false);

            SelectionKey clientKey = clientChannel.register(selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从通道中获取请求并进行包装
     *
     * @param selectionKey
     * @return
     * @throws IOException
     */
    private MyRequest getRequest(SelectionKey selectionKey) throws IOException {
        return new MyRequest(selectionKey);    //  包装request
    }

    /**
     * 从通道中获取响应并进行包装
     *
     * @param selectionKey
     * @return
     */
    private MyResponse getResponse(SelectionKey selectionKey) {
        return new MyResponse(selectionKey);     //  包装response
    }

    /**
     * 初始化Servlet的映射对象
     */
    private void initServletMapping() {
        for (ServletMapping servletMapping : ServletMappingConfig.servletMappingList) {
            urlServletMap.put(servletMapping.getUrl(), servletMapping.getClazz());
        }
    }

    /**
     * 请求调度
     *
     * @param myRequest
     * @param myResponse
     */
    private void dispatch(MyRequest myRequest, MyResponse myResponse) {
        if (myRequest == null) return;
        if (myResponse == null) return;
        String clazz = urlServletMap.get(myRequest.getUrl());

        try {
            if (clazz == null) {
                myResponse.write("404");
                return;
            }
            es.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Class<MyServlet> myServletClass = (Class<MyServlet>) Class.forName(clazz);
                        MyServlet myServlet = myServletClass.newInstance();
                        myServlet.service(myRequest, myResponse);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        new MyTomcat().start();
    }
}
