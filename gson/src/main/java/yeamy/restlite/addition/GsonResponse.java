package yeamy.restlite.addition;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class GsonResponse extends AbstractHttpResponse<Object> {
    private static final DateFormat DF = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateFormat TF = new SimpleDateFormat("HH:mm:ss");
    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss X")
            .registerTypeAdapter(BigDecimal.class, (JsonSerializer<BigDecimal>)
                    (src, typeOfSrc, context) -> new JsonPrimitive(src.toPlainString()))
            .registerTypeAdapter(Date.class, (JsonSerializer<Date>)
                    (src, typeOfSrc, context) -> new JsonPrimitive(DF.format(src)))
            .registerTypeAdapter(Time.class, (JsonSerializer<Time>)
                    (src, typeOfSrc, context) -> new JsonPrimitive(TF.format(src)))
            .create();

    public GsonResponse(Object data) {
        this(200, data);
    }

    public GsonResponse(int status, Object data) {
        super(data);
        setStatus(status);
        setContentType("application/json");
    }

    protected String toJSON() {
        return gson.toJson(getData());
    }

    @Override
    protected void writeContent(HttpServletResponse resp) throws IOException {
        PrintWriter w = resp.getWriter();
        w.write(toJSON());
        w.close();
    }

}
