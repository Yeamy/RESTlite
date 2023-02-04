package example;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import yeamy.restlite.annotation.Position;

import java.io.IOException;

@Position(4)
@WebFilter("/*")
public class TestFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        System.out.println("------------2");
        chain.doFilter(request, response);
    }
}
