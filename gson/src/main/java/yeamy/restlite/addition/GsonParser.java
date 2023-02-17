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
import java.sql.Time;
import java.text.SimpleDateFormat;

/**
 * gson with date format yyyy-MM-dd HH:mm:ss X
 */
public class GsonParser {
    private static final ThreadLocal<Gson> gsonLocal = new ThreadLocal<>();
    private static volatile GsonBuilder gsonBuilder = new GsonBuilder()
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
            .registerTypeAdapter(java.util.Date.class, new TypeAdapter<java.util.Date>() {
                final SimpleDateFormat SF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                @Override
                public void write(JsonWriter out, java.util.Date value) throws IOException {
                    out.value(SF.format(value));
                }

                @Override
                public java.util.Date read(JsonReader in) throws IOException {
                    try {
                        return SF.parse(in.nextString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            })
            .registerTypeAdapter(java.sql.Date.class, new TypeAdapter<java.sql.Date>() {
                final SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd");
                @Override
                public void write(JsonWriter out, java.sql.Date value) throws IOException {
                    out.value(DF.format(value));
                }

                @Override
                public java.sql.Date read(JsonReader in) throws IOException {
                    try {
                        return new java.sql.Date(DF.parse(in.nextString()).getTime());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            })
            .registerTypeAdapter(Time.class, new TypeAdapter<Time>() {
                final SimpleDateFormat TF = new SimpleDateFormat("HH:mm:ss");
                @Override
                public void write(JsonWriter out, Time value) throws IOException {
                    out.value(TF.format(value));
                }

                @Override
                public Time read(JsonReader in) throws IOException {
                    try {
                        return new Time(TF.parse(in.nextString()).getTime());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
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