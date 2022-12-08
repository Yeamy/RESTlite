package yeamy.restlite.addition;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import jakarta.servlet.http.HttpServletResponse;

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
		try(OutputStream os = resp.getOutputStream()) {
			os.write(getData().getBytes(getCharset()));
		}
	}

}