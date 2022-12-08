package yeamy.restlite.addition;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;

import static yeamy.restlite.addition.GsonParser.gson;

public class GsonResponse extends AbstractHttpResponse<Object> {

    public GsonResponse(Object data) {
        this(200, data);
    }

    public GsonResponse(int status, Object data) {
        super(data);
        setStatus(status);
        setContentType("application/json");
    }

    protected String toJSON() {
        return gson.toJson(getData());
    }

    @Override
    protected void writeContent(HttpServletResponse resp) throws IOException {
        try(OutputStream os = resp.getOutputStream()) {
            os.write(toJSON().getBytes(getCharset()));
        }
    }

}