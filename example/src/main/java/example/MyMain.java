package example;

import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import yeamy.utils.TextUtils;

public class MyMain {
    // tomcat对象
    private static final Tomcat tomcat = new Tomcat();

    // 初始化tomcat容器
    static {
        tomcat.setHostname("localhost");
        tomcat.setBaseDir("/Users/Yeamy0754/");
        tomcat.setPort(80);
        Connector conn = tomcat.getConnector();
        conn.setURIEncoding("UTF-8");// tomcat的字符编码集
        // 设置Host
        Host host = tomcat.getHost();
//        host.setAppBase("/Users/Yeamy0754/");

        // 获取目录绝对路径
//        String baseDir = System.getProperty("user.dir");

        Context context = tomcat.addContext(host, "", System.getProperty("user.dir"));
//        context.setManager(new NoSessionManager());
        context.setApplicationEventListeners(new Object[]{
                new RESTliteWebListener()
        });
        FilterDef fd;
        FilterMap fm;
        fd = new FilterDef();
        fd.setFilterName("RESTliteWebFilter");
        fd.setFilter(new RESTliteWebFilter());
        context.addFilterDef(fd);
        fm = new FilterMap();
        fm.setFilterName("RESTliteWebFilter");
        fm.addURLPattern("/*");
        context.addFilterMap(fm);

//      HttpServlet对象是你现有的自定义的Servlet容器
        HttpServlet[] servlets = new HttpServlet[]{new OkServlet(), new NoServlet()};
        for (HttpServlet servlet : servlets) {
            WebServlet webServlet = servlet.getClass().getAnnotation(WebServlet.class);
            String[] uris = webServlet.value();
            String name = webServlet.name();
            if (TextUtils.isEmpty(name)) name = servlet.getClass().getSimpleName();
            Wrapper wrapper = tomcat.addServlet("", name, servlet);
            wrapper.setMultipartConfigElement(new MultipartConfigElement(OkServlet.class.getAnnotation(MultipartConfig.class)));

            for (String uri : uris) {
                wrapper.addMapping(uri);
            }
        }
    }

    public static void main(String[] args) {
        try {
            // 启动tomcat
            tomcat.start();
            // 保持tomcat的启动状态
            tomcat.getServer().await();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
