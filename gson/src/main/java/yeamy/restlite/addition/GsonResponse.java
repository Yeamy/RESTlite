package yeamy.restlite.addition;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * JSON response serialize by Gson
 */
public class GsonResponse extends AbstractHttpResponse<Object> {

    /**
     * with default http status success code 200
     *
     * @param data http body
     */
    public GsonResponse(Object data) {
        this(200, data);
    }

    /**
     * @param status http status code
     * @param data   http body
     */
    public GsonResponse(int status, Object data) {
        super(data);
        setStatus(status);
        setContentType("application/json");
    }

    protected String toJSON() {
        return GsonParser.toJSON(getData());
    }

    @Override
    protected void writeContent(HttpServletResponse resp) throws IOException {
        try (PrintWriter w = resp.getWriter()) {
            w.write(toJSON());
        }
    }

}
