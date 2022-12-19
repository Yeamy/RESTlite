package yeamy.restlite.addition;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import yeamy.restlite.RESTfulRequest;

import java.io.IOException;
import java.text.SimpleDateFormat;

public class JacksonParser {
    private static volatile ObjectMapper jackson = new ObjectMapper()
            .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss X"));

    public static void setJackson(ObjectMapper jackson) {
        JacksonParser.jackson = jackson;
    }

    public static <T> T parse(RESTfulRequest request, Class<T> clz) throws IOException {
        return jackson.readValue(request.getBodyAsText(), clz);
    }

    public static String toJSON(Object obj) throws JsonProcessingException {
        return jackson.writeValueAsString(obj);
    }
}