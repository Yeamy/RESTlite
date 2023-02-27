package example;

import bean.*;
import yeamy.restlite.addition.TextPlainResponse;
import yeamy.restlite.annotation.*;

@RESTfulResource("example")
public class ExampleMain {
    @Inject
    InjectField field;
    @Inject
    InjectStaticMethod method;
    @Inject
    InjectConstructor constructor;
    @Inject
    InjectCreator creator;

	@GET
	public String get(@Inject InjectParam ij, String p) {
		return "get";
	}

//	@GET
//	public String get(@Param(required = false) String p) {
//		return "get";
//	}

//    @POST
//    public String post1(@Body(creator = "yeamy.restlite.addition.GsonParser") ExampleBody bean) {
//        return null;
//    }

//    @POST
//    public String post2(@Param String p1, @Cookies String c, @Header String h, @GsonBody int b2) {
//        return null;
//    }

//    @POST
//    public String post3(@Param String p1) {
//        return null;
//    }

//    @POST
//    public ArrayList<String> post3(String p1, String p2) {
//        return null;
//    }

    @ERROR
    public Object error(Exception e) {
        return new TextPlainResponse(e.toString());
    }

//    @POST
//    public String post3(@Body(creator = "xxx.XXX") ExampleBody bean) {
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
