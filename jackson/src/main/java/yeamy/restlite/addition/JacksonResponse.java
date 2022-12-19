package yeamy.restlite.addition;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;

public class JacksonResponse extends AbstractHttpResponse<Object> {

    public static volatile ObjectMapper jackson = new ObjectMapper()
            .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss X"));

    public JacksonResponse(Object data) {
        this(200, data);
    }

    public JacksonResponse(int status, Object data) {
        super(data);
        setStatus(status);
        setContentType("application/json");
    }

    protected String toJSON() throws JsonProcessingException {
        return jackson.writeValueAsString(getData());
    }

    @Override
    protected void writeContent(HttpServletResponse resp) throws IOException {
        try(OutputStream os = resp.getOutputStream()) {
            os.write(toJSON().getBytes(getCharset()));
        }
    }

}
