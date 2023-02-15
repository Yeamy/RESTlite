package yeamy.restlite.addition;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import yeamy.restlite.RESTfulRequest;
import yeamy.restlite.annotation.LinkTag;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * gson with date format yyyy-MM-dd HH:mm:ss X
 */
public class GsonParser {
    private static final ThreadLocal<Gson> gsonLocal = new ThreadLocal<>();
    private static volatile GsonBuilder gsonBuilder = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss X")
            .registerTypeAdapter(BigDecimal.class, new TypeAdapter<BigDecimal>() {
                @Override
                public void write(JsonWriter out, BigDecimal value) throws IOException {
                    out.value(value.toPlainString());
                }

                @Override
                public BigDecimal read(JsonReader in) throws IOException {
                    return new BigDecimal(in.nextString());
                }
            })
            .registerTypeAdapter(Date.class, new TypeAdapter<Date>() {
                final SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd");

                @Override
                public void write(JsonWriter out, Date src) throws IOException {
                    out.value(DF.format(src));
                }

                @Override
                public Date read(JsonReader in) throws IOException {
                    try {
                        return new Date(DF.parse(in.nextString()).getTime());
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
            })
            .registerTypeAdapter(Time.class, new TypeAdapter<Time>() {
                final SimpleDateFormat TF = new SimpleDateFormat("HH:mm:ss");

                @Override
                public void write(JsonWriter out, Time src) throws IOException {
                    out.value(TF.format(src));
                }

                @Override
                public Time read(JsonReader in) throws IOException {
                    try {
                        return new Time(TF.parse(in.nextString()).getTime());
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

    public static Gson getGson() {
        Gson gson = gsonLocal.get();
        if (gson == null) {
            gsonLocal.set(gson = gsonBuilder.create());
        }
        return gson;
    }

    /**
     * replace the gson
     */
    public static void setGsonBuilder(GsonBuilder gsonBuilder) {
        GsonParser.gsonBuilder = gsonBuilder;
    }

    /**
     * deserializes request body as JSON into an object of the specified class.
     */
    @LinkTag("deserializes")
    public static <T> T parse(RESTfulRequest request, Class<T> clz) {
        return getGson().fromJson(request.getBodyAsText(), clz);
    }

    /**
     * serializes the given object to JSON
     */
    public static String toJSON(Object data) {
        return getGson().toJson(data);
    }
}