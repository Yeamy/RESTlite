package bean;

import yeamy.restlite.addition.GsonParser;
import yeamy.restlite.annotation.Body;

@Body(processor = GsonParser.class)
public class ExampleBody {
	public String value;
}
