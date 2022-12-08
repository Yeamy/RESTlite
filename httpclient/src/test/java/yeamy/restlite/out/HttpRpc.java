package yeamy.restlite.out;

import yeamy.restlite.annotation.Param;
import yeamy.restlite.httpclient.RESTRequest;
import yeamy.restlite.httpclient.Values;

@RESTRequest(
        host = "http://localhost:8080",
        cookie = @Values(name = "11", value = "22"),
        param = {@Values(name = "1", value = "2"),
                @Values(name = "3", value = "4")})
public interface HttpRpc {

    @RESTRequest(resource = "a")
    String get();

    @RESTRequest(resource = "a", method = "POST")
    String post(@Param String a);
}
