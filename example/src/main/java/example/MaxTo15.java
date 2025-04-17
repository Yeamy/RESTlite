package example;

import yeamy.restlite.addition.TextPlainResponse;
import yeamy.restlite.addition.ProcessException;
import yeamy.restlite.annotation.ParamProcessor;

public class MaxTo15 {

    @ParamProcessor("maxTo15")
    public static long maxTo15(String str) throws ProcessException {
        try {
            int ok = Integer.parseInt(str);
            return Math.min(ok, 15);
        } catch (Exception e) {
            throw new ProcessException(new TextPlainResponse(500, "xxx"));
        }
    }
}
