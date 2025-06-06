package example;

import jakarta.annotation.Priority;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;

@Priority(4)
@WebFilter("/*")
public class TestFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        System.out.println("------------2");
        HttpServletRequest req = (HttpServletRequest) request;
        System.out.println(req.getMethod());
        System.out.println(req.getRequestURL());
        Enumeration<String> headers = req.getHeaderNames();
        while (headers.hasMoreElements()) {
            String hn = headers.nextElement();
            Enumeration<String> hs = req.getHeaders(hn);
            while (hs.hasMoreElements()) {
                String hv = hs.nextElement();
                System.out.println(hn + " : " + hv);
            }
        }
        Map<String, String[]> map = req.getParameterMap();
        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            System.out.println(entry.getKey() + "     " + Arrays.toString(entry.getValue()));
        }

        chain.doFilter(request, response);
    }
}
