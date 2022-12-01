package example;

import jakarta.servlet.http.HttpServletResponse;
import yeamy.restlite.addition.AbstractHttpResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class MyResponse extends AbstractHttpResponse<Object> {

    public MyResponse(Object data) {
        super(data);
    }

    @Override
    protected void writeContent(HttpServletResponse resp) throws IOException {
        try (PrintWriter w = resp.getWriter()) {
            w.write(Configuration.gson.toJson(getData()));
        }
    }
}
