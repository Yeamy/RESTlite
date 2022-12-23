package bean;

import yeamy.restlite.annotation.Body;

@Body(creator = "yeamy.restlite.addition.GsonParser")
public class ExampleBean {
	public String value;
}
