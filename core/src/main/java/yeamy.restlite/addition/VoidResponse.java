package yeamy.restlite.addition;

import jakarta.servlet.http.HttpServletResponse;
import yeamy.restlite.HttpResponse;

import java.io.IOException;

public class VoidResponse implements HttpResponse {
    public final static VoidResponse instance = new VoidResponse();
    private final int status;

    public VoidResponse() {
        this(200);
    }

    public VoidResponse(int status) {
        this.status = status;
    }

    @Override
    public void write(HttpServletResponse resp) throws IOException {
        resp.setStatus(status);
    }

}