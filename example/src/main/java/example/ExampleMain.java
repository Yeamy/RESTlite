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
//	public String get(@Inject InjectA injectA, @Param(processor = "maxTo15") long size, String p) {
//		return "get";
//	}

	public String get(@Inject InjectA injectA, @MaxTo15Param long longSize, String p) {
		return "get";
	}

	@GET
	public String get2(@Param int size, String p) {
		return "get";
	}

	@GET
	public String get(@Param(required = false) String p) {
		return "get";
	}

//    @POST
//    public String post1(@Body(processor = "") ExampleBody bean) {
//        return null;
//    }

    @POST
    public String post1(@GsonBody ExampleBody bean) {
        return null;
    }

    @POST
    public String post1(int oo, @GsonPart("part1") ExamplePart bean) {
        return null;
    }

    @POST
    public String post2(@Param String p1, @Cookies String c, @Header String h, @GsonBody int b2) {
        return null;
    }

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

//	@GET
//	public String get(GsonFeign client) {
//		return null;
//	}

//	public String get(ExampleClient client) {
//		return client.get().value;
//	}

}
