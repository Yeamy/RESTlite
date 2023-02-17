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
import java.util.Date;

/**
 * jackson with date format yyyy-MM-dd HH:mm:ss X
 */
public class JacksonParser {
    private static final ThreadLocal<ObjectMapper> local = new ThreadLocal<>();

    private static class DateFormatModule extends SimpleModule {
        public DateFormatModule() {
            final SimpleDateFormat TF = new SimpleDateFormat("HH:mm:ss");
            final SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd");
            final SimpleDateFormat SF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            addSerializer(Time.class, new JsonSerializer<>() {

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
            addSerializer(java.sql.Date.class, new JsonSerializer<>() {

                @Override
                public void serialize(java.sql.Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                    gen.writeString(DF.format(value));
                }
            });
            addDeserializer(java.sql.Date.class, new JsonDeserializer<>() {
                @Override
                public java.sql.Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
                    try {
                        return new java.sql.Date(DF.parse(p.getValueAsString()).getTime());
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            addSerializer(Date.class, new JsonSerializer<>() {

                @Override
                public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                    gen.writeString(SF.format(value));
                }
            });
            addDeserializer(Date.class, new JsonDeserializer<>() {
                @Override
                public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
                    try {
                        return SF.parse(p.getValueAsString());
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }

    private static volatile JacksonBuilder builder = () -> new ObjectMapper()
            .registerModule(new DateFormatModule());

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