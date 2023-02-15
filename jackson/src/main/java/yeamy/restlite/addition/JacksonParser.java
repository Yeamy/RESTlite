package yeamy.restlite.addition;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import yeamy.restlite.RESTfulRequest;

import java.io.IOException;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * jackson with date format yyyy-MM-dd HH:mm:ss X
 */
public class JacksonParser {
    private static final ThreadLocal<ObjectMapper> local = new ThreadLocal<>();

    private static class DateFormatModule extends SimpleModule {
        public DateFormatModule() {
            final SimpleDateFormat TF = new SimpleDateFormat("HH:mm:ss");
            addSerializer(Time.class, new JsonSerializer<>() {
                final SimpleDateFormat TF = new SimpleDateFormat("HH:mm:ss");

                @Override
                public void serialize(Time value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                    gen.writeString(TF.format(value));
                }
            });
            addDeserializer(Time.class, new JsonDeserializer<>() {
                @Override
                public Time deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
                    try {
                        return new Time(TF.parse(p.getValueAsString()).getTime());
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }

    private static volatile JacksonBuilder builder = () -> new ObjectMapper()
            .registerModule(new DateFormatModule())
            .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss X"));

    private static ObjectMapper getJackson() {
        ObjectMapper jackson = local.get();
        if (jackson == null) {
            local.set(jackson = builder.build());
        }
        return jackson;
    }

    /**
     * replace the jackson
     */
    public static void setJacksonBuilder(JacksonBuilder builder) {
        JacksonParser.builder = builder;
    }

    /**
     * deserializes request body as JSON into an object of the specified class.
     */
    public static <T> T parse(RESTfulRequest request, Class<T> clz) throws IOException {
        return getJackson().readValue(request.getBodyAsText(), clz);
    }

    /**
     * serializes the given object to JSON
     */
    public static String toJSON(Object obj) throws JsonProcessingException {
        return getJackson().writeValueAsString(obj);
    }
}