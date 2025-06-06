package yeamy.restlite.addition;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.apache.commons.lang3.time.FastDateFormat;
import yeamy.restlite.annotation.BodyProcessor;
import yeamy.restlite.annotation.PartProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * gson with date format yyyy-MM-dd HH:mm:ss
 */
public class GsonParser {
    private static final FastDateFormat TF = FastDateFormat.getInstance("HH:mm:ss", TimeZone.getDefault(), Locale.getDefault());
    private static final FastDateFormat DF = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getDefault(), Locale.getDefault());
    private static final FastDateFormat SF = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss", TimeZone.getDefault(), Locale.getDefault());
    private static volatile Gson gson = new GsonBuilder()
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
            .registerTypeAdapter(Date.class, new TypeAdapter<Date>() {
                @Override
                public void write(JsonWriter out, Date value) throws IOException {
                    out.value(DF.format(value));
                }

                @Override
                public Date read(JsonReader in) throws IOException {
                    try {
                        return new Date(DF.parse(in.nextString()).getTime());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            })
            .registerTypeAdapter(Timestamp.class, new TypeAdapter<Timestamp>() {
                @Override
                public void write(JsonWriter out, Timestamp value) throws IOException {
                    out.value(DF.format(value));
                }

                @Override
                public Timestamp read(JsonReader in) throws IOException {
                    try {
                        return new Timestamp(DF.parse(in.nextString()).getTime());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            })
            .registerTypeAdapter(Time.class, new TypeAdapter<Time>() {
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
            }).create();

    /**
     * replace the gson
     */
    public static void setGson(Gson gson) {
        GsonParser.gson = gson;
    }

    @BodyProcessor("gsonBody")
    @PartProcessor("gsonPart")
    public static <T> T fromJson(InputStream json, Type clz) {
        if (json == null) return null;
        return gson.fromJson(new InputStreamReader(json), clz);
    }

    /**
     * serializes the given object to JSON
     */
    public static String toJSON(Object data) {
        return gson.toJson(data);
    }
}