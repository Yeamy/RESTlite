package yeamy.restlite.addition;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import yeamy.restlite.RESTfulRequest;

import java.io.IOException;
import java.sql.Time;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * jackson with date format yyyy-MM-dd HH:mm:ss
 */
public class JacksonXmlParser {
    private static volatile XmlMapper mapper = (XmlMapper) new XmlMapper().registerModule(new DateFormatModule());
    private static final FastDateFormat TF = FastDateFormat.getInstance("HH:mm:ss", TimeZone.getDefault(), Locale.getDefault());
    private static final FastDateFormat DF = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getDefault(), Locale.getDefault());
    private static final FastDateFormat SF = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss", TimeZone.getDefault(), Locale.getDefault());

    private static class DateFormatModule extends SimpleModule {
        public DateFormatModule() {
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

    /**
     * replace the jackson
     */
    public static void setXmlJackson(XmlMapper mapper) {
        JacksonXmlParser.mapper = mapper;
    }

    /**
     * deserializes request body as JSON into an object of the specified class.
     */
    public static <T> T parse(RESTfulRequest request, Class<T> clz) throws IOException {
        return mapper.readValue(request.getBodyAsText(), clz);
    }

    /**
     * serializes the given object to JSON
     */
    public static String toXml(Object obj) throws JsonProcessingException {
        return mapper.writeValueAsString(obj);
    }
}