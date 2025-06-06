package yeamy.restlite.addition;

import com.alibaba.fastjson2.JSONObject;
import yeamy.restlite.annotation.BodyProcessor;
import yeamy.restlite.annotation.PartProcessor;

import java.lang.reflect.Type;

public class FastjsonParser {

    @BodyProcessor("fastjsonBody")
    @PartProcessor("fastjsonPart")
    public static <T> T fromJson(String json, Type clz) {
        return json == null ? null : JSONObject.parseObject(json, clz);
    }

    /**
     * serializes the given object to JSON
     */
    public static String toJSON(Object data) {
        return JSONObject.toJSONString(data);
    }

}