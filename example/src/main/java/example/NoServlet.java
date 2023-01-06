package example;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletResponse;
import yeamy.restlite.RESTfulRequest;
import yeamy.restlite.RESTfulServlet;
import yeamy.restlite.addition.TextPlainResponse;

import java.io.IOException;

@WebServlet({"/no", "/noo"})
public class NoServlet extends RESTfulServlet {
    @Override
    public void doGet(RESTfulRequest req, HttpServletResponse resp) throws IOException {
        new TextPlainResponse("no " + req.getServerName()).write(resp);
    }
}
