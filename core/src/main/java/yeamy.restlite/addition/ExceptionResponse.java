package yeamy.restlite.addition;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class ExceptionResponse extends AbstractHttpResponse<Exception> {

	public ExceptionResponse(Exception e) {
		super(e);
		setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	}

	@Override
	protected void writeContent(HttpServletResponse resp) throws IOException {
		try (PrintWriter w = resp.getWriter()) {
			getData().printStackTrace(w);
		}
	}

}