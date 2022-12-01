package yeamy.restlite.addition;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

public class ExceptionResponse extends AbstractHttpResponse<Exception> {

	public ExceptionResponse(Exception e) {
		super(e);
		setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	}

	@Override
	protected void writeContent(HttpServletResponse resp) throws IOException {
		PrintWriter w = resp.getWriter();
		getData().printStackTrace(w);
		w.close();
	}

}