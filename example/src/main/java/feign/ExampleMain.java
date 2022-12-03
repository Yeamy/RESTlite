package feign;

import yeamy.restlite.addition.GsonBody;
import yeamy.restlite.annotation.Body;
import yeamy.restlite.annotation.GET;
import yeamy.restlite.annotation.POST;
import yeamy.restlite.annotation.Resource;

@Resource("example")
public class ExampleMain {

	@GET
	public String get(@Body ExampleBean bean) {
		return null;
	}

	@POST
	public String get2(@GsonBody ExampleBean bean) {
		return null;
	}

//	@GET
//	public String get(FeignImpl client) {
//		return null;
//	}

//	public String get(ExampleClient client) {
//		return client.get().value;
//	}

}
