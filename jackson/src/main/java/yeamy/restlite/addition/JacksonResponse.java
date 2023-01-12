package yeamy.restlite.addition;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * JSON response serialize by Jackson
 */
public class JacksonResponse extends AbstractHttpResponse<Object> {

    /**
     * with default http status success code 200
     * @param data   http body
     */
    public JacksonResponse(Object data) {
        this(200, data);
    }

    /**
     * @param status http status code
     * @param data   http body
     */
    public JacksonResponse(int status, Object data) {
        super(data);
        setStatus(status);
        setContentType("application/json");
    }

    protected String toJSON() throws JsonProcessingException {
        return JacksonParser.toJSON(getData());
    }

    @Override
    protected void writeContent(HttpServletResponse resp) throws IOException {
        try (PrintWriter w = resp.getWriter()) {
            w.write(toJSON());
        }
    }

}
