package yeamy.restlite.addition;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * XML response serialize by Jackson
 */
public class JacksonXmlResponse extends AbstractHttpResponse<Object> {

    /**
     * with default http status success code 200
     * @param data   http body
     */
    public JacksonXmlResponse(Object data) {
        this(200, data);
    }

    /**
     * @param status http status code
     * @param data   http body
     */
    public JacksonXmlResponse(int status, Object data) {
        super(data);
        setStatus(status);
        setContentType("application/json");
    }

    protected String toXml() throws JsonProcessingException {
        return JacksonXmlParser.toXml(getData());
    }

    @Override
    protected void writeContent(HttpServletResponse resp) throws IOException {
        try (PrintWriter w = resp.getWriter()) {
            w.write(toXml());
        }
    }

}
