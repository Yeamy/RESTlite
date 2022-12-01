package feign;

import yeamy.restlite.annotation.Extra;
import yeamy.restlite.annotation.GET;
import yeamy.restlite.annotation.Resource;

@Resource("example")
public class ExampleMain {

	@GET
	public String get(@Extra FeignImpl client) {
		return null;
	}

//	public String get(@Extra ExampleClient client) {
//		return client.get().value;
//	}

}
