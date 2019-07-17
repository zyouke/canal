package com.zyouke.tomcat;

import java.util.ArrayList;
import java.util.List;

/**
 * @author beifengtz
 * <a href='http://www.beifengtz.com'>www.beifengtz.com</a>
 * <p>location: mytomcat.javase_learning</p>
 * Created in 15:01 2019/4/21
 */
public class ServletMappingConfig {
    public static List<ServletMapping> servletMappingList = new ArrayList<>();

    static {
        servletMappingList.add(new ServletMapping("helloWorld","/world","com.zyouke.tomcat.HelloWorldServlet"));
    }
}
