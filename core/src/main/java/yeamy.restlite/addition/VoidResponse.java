package yeamy.restlite.addition;

import jakarta.servlet.http.HttpServletResponse;
import yeamy.restlite.HttpResponse;

import java.io.IOException;

public class VoidResponse implements HttpResponse {
    public final static VoidResponse NO_CONTENT = new VoidResponse(204);
    public final static VoidResponse RESET_CONTENT = new VoidResponse(205);
    private final int status;

    private VoidResponse(int status) {
        this.status = status;
    }

    @Override
    public void write(HttpServletResponse resp) throws IOException {
        resp.setStatus(status);
    }

}