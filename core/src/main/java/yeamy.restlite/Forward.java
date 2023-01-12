package yeamy.restlite;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import yeamy.restlite.addition.ExceptionResponse;

import java.io.IOException;

/**
 * Forwards a request to another resource
 *
 * @see RequestDispatcher#forward(ServletRequest, ServletResponse)
 */
public class Forward implements HttpResponse {
    private final RESTfulRequest req;

    public Forward(RESTfulRequest req, String resource) {
        this.req = req.getForward(resource);
    }

    public Forward setParameter(String name, String value) {
        req.addParameter(name, value);
        return this;
    }

    @Override
    public void write(HttpServletResponse resp) throws IOException {
        HttpServletRequest request = req.getRequest();
        try {
            request.getRequestDispatcher(req.getResource()).forward(request, resp);
        } catch (ServletException e) {
            e.printStackTrace();
            new ExceptionResponse(e).write(resp);
        }
    }
}