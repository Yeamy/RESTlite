package yeamy.restlite.addition;

import java.io.IOException;

import jakarta.servlet.http.HttpServletResponse;

import yeamy.restlite.HttpResponse;

public class VoidResponse implements HttpResponse {
	private final int status;

	public VoidResponse() {
		this(200);
	}

	public VoidResponse(int status) {
		this.status = status;
	}

	@Override
	public void write(HttpServletResponse resp) throws IOException {
		resp.setStatus(status);
	}

}