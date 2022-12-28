package example;

import bean.ExampleBean;
import yeamy.restlite.HttpResponse;
import yeamy.restlite.addition.TextPlainResponse;
import yeamy.restlite.annotation.*;

import java.util.ArrayList;

@Resource("example")
public class ExampleMain {
    //	ExampleClient client;
//
//	@GET
//	public String get(String p) {
//		return null;
//	}
    @POST
    public String post1(@Body(creator = "yeamy.restlite.addition.GsonParser") ExampleBean bean) {
        return null;
    }

//    @POST
//    public String post2(@Param String p1, @GsonBody int b2) {
//        return null;
//    }

    @POST
    public String post3(@Param String p1) {
        return null;
    }

    @POST
    public ArrayList<String> post3(String p1, String p2) {
        return null;
    }

    @ERROR
    public Object error(Exception e) {
        return new TextPlainResponse(e.toString());
    }

//    @POST
//    public String post3(@Body(creator = "xxx.XXX") ExampleBean bean) {
//        return null;
//    }

//	@GET
//	public String get(GsonFeign client) {
//		return null;
//	}

//	public String get(ExampleClient client) {
//		return client.get().value;
//	}

}
