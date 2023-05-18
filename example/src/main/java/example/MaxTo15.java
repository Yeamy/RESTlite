package example;

import yeamy.restlite.RESTfulRequest;

public class MaxTo15 {

    public static long maxTo15(RESTfulRequest req) {
        int ok = req.getIntParam("ok");
        return Math.min(ok, 15);
    }

    public static int maxTo15(String str) {
        int ok = Integer.parseInt(str);
        return Math.min(ok, 15);
    }
}
