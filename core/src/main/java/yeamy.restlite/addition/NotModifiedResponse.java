package yeamy.restlite.addition;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import yeamy.restlite.HttpResponse;

public class NotModifiedResponse implements HttpResponse {

	@Override
	public void write(HttpServletResponse resp) throws IOException {
		resp.setStatus(304);
	}

}