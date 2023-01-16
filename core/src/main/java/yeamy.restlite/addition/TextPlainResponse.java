package yeamy.restlite.addition;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class TextPlainResponse extends AbstractHttpResponse<Object> {

	public TextPlainResponse(Object txt) {
		this(200, txt);
	}

	public TextPlainResponse(int status, Object txt) {
		super(txt);
		setStatus(status);
		setContentType("text/plain");
	}

	@Override
	protected void writeContent(HttpServletResponse resp) throws IOException {
		try (PrintWriter w = resp.getWriter()) {
			w.write(String.valueOf(getData()));
		}
	}

}
