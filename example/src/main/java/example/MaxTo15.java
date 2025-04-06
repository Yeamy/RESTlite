package example;

import yeamy.restlite.RESTfulRequest;
import yeamy.restlite.annotation.ParamProcessor;

public class MaxTo15 {

    @ParamProcessor("maxTo15")
    public static long maxTo15(String str) {
        int ok = Integer.parseInt(str);
        return Math.min(ok, 15);
    }
}
