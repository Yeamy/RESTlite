package yeamy.restlite.addition;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import yeamy.restlite.RESTfulRequest;

import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * jackson with date format yyyy-MM-dd HH:mm:ss X
 */
public class JacksonXmlParser {
    private static volatile ObjectMapper jackson = new XmlMapper()
            .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss X"));

    /**
     * replace the jackson
     */
    public static void setJackson(ObjectMapper jackson) {
        JacksonXmlParser.jackson = jackson;
    }

    /**
     * deserializes request body as XML into an object of the specified class.
     */
    public static <T> T parse(RESTfulRequest request, Class<T> clz) throws IOException {
        return jackson.readValue(request.getBodyAsText(), clz);
    }

    /**
     * serializes the given object to XML
     */
    public static String toXml(Object obj) throws JsonProcessingException {
        return jackson.writeValueAsString(obj);
    }
}