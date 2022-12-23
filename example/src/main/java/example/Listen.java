package example;

import yeamy.restlite.RESTfulListener;
import yeamy.restlite.RESTfulRequest;

public class Listen extends RESTfulListener {

    @Override
    public String createServerName(RESTfulRequest _req) {
        switch (super.createServerName(_req)) {
            case "abc:GET":
                if (_req.has("a")&&_req.has("b")) {
                    return "abc:GET:a,b";
                }
                break;
            case "abc:POST":
                if (_req.has("b")&&_req.has("c")) {
                    return "abc:GET:b,c";
                }
        }
        return "";
    }
}
