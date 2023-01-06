package example;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import yeamy.restlite.RESTfulRequest;
import yeamy.restlite.RESTfulServlet;
import yeamy.restlite.addition.TextPlainResponse;

import java.io.IOException;

@WebServlet("/ok")
public class OkServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        new TextPlainResponse("ok " + req.getServerName()).write(resp);
    }
}
