package com.zyouke.tomcat;

/**
 * @author beifengtz
 * <a href='http://www.beifengtz.com'>www.beifengtz.com</a>
 * <p>location: mytomcat.javase_learning</p>
 * Created in 14:53 2019/4/21
 */
public abstract class MyServlet {

    public abstract void doGet(MyRequest myRequest,MyResponse myResponse);

    public abstract void doPost(MyRequest myRequest,MyResponse myResponse);

    public void service(MyRequest myRequest,MyResponse myResponse){
        if (myRequest.getMethod().equalsIgnoreCase("POST")){
            doPost(myRequest,myResponse);
        }else if (myRequest.getMethod().equalsIgnoreCase("GET")){
            doGet(myRequest,myResponse);
        }
    }
}
