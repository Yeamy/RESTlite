package yeamy.restlite.addition;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import yeamy.restlite.RESTfulRequest;
import yeamy.restlite.annotation.LinkTag;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * gson with date format yyyy-MM-dd HH:mm:ss X
 */
public class GsonParser {
    private static final DateFormat DF = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateFormat TF = new SimpleDateFormat("HH:mm:ss");
    private static volatile Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss X")
            .registerTypeAdapter(BigDecimal.class, (JsonSerializer<BigDecimal>)
                    (src, typeOfSrc, context) -> new JsonPrimitive(src.toPlainString()))
            .registerTypeAdapter(Date.class, (JsonSerializer<Date>)
                    (src, typeOfSrc, context) -> new JsonPrimitive(DF.format(src)))
            .registerTypeAdapter(Time.class, (JsonSerializer<Time>)
                    (src, typeOfSrc, context) -> new JsonPrimitive(TF.format(src)))
            .create();

    /**
     * replace the gson
     */
    public static void setGson(Gson gson) {
        GsonParser.gson = gson;
    }

    /**
     * deserializes request body as JSON into an object of the specified class.
     */
    @LinkTag("deserializes")
    public static <T> T parse(RESTfulRequest request, Class<T> clz) {
        return gson.fromJson(request.getBodyAsText(), clz);
    }

    /**
     * serializes the given object to JSON
     */
    public static String toJSON(Object data) {
        return gson.toJson(data);
    }
}