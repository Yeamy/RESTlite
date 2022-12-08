package yeamy.restlite.out;

import yeamy.restlite.httpclient.HttpClientImpl;
import yeamy.restlite.httpclient.ClientRequest;

public class HttpRpcImpl implements HttpRpc {

    public static ClientRequest getRequest() {
        ClientRequest bean = new ClientRequest();
        bean.baseUri = "";
        return bean;
    }

    //    @RESTRequest(resource = "a")
    @Override
    public String get() {
        String uri = "";
        ClientRequest r = getRequest();
        r.resource = "a";
        return HttpClientImpl.execute(r, null);
    }

    //    @RESTRequest(resource = "a", method = "POST")
    @Override
    public String post(String a) {
        String uri = ""+"/a/"+a;
        return "";
    }
}
