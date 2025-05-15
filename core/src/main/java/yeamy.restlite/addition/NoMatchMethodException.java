package yeamy.restlite.addition;

import yeamy.restlite.RESTfulRequest;

import java.io.Serial;

public class NoMatchMethodException extends Exception {
	@Serial
	private static final long serialVersionUID = -6391725202573079565L;

	public NoMatchMethodException(RESTfulRequest req) {
		super("no match service found! " + req.getRequest().getRequestURI());
	}

}
