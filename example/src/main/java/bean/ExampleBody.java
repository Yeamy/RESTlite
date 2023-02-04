package bean;

import yeamy.restlite.addition.GsonParser;
import yeamy.restlite.annotation.Body;

@Body(creator = GsonParser.class)
public class ExampleBody {
	public String value;
}
