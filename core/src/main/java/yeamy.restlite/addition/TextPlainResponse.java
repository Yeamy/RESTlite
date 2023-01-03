package yeamy.restlite.addition;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class TextPlainResponse extends AbstractHttpResponse<String> {

	public TextPlainResponse(String txt) {
		this(200, txt);
	}

	public TextPlainResponse(int status, String txt) {
		super(txt);
		setStatus(status);
		setContentType("text/plain");
	}

	@Override
	protected void writeContent(HttpServletResponse resp) throws IOException {
		try (PrintWriter w = resp.getWriter()) {
			w.write(getData());
		}
	}

}
