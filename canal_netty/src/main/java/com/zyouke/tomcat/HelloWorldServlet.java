package com.zyouke.tomcat;

import java.io.IOException;

/**
 * @author beifengtz
 * <a href='http://www.beifengtz.com'>www.beifengtz.com</a>
 * <p>location: mytomcat.javase_learning</p>
 * Created in 14:57 2019/4/21
 */
public class HelloWorldServlet extends MyServlet {
    @Override
    public void doGet(MyRequest myRequest, MyResponse myResponse) {
        try{
            myResponse.write("get hello world");
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void doPost(MyRequest myRequest, MyResponse myResponse) {
        try{
            myResponse.write("post hello world");
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
