package yeamy.restlite;

import java.io.IOException;

import jakarta.servlet.http.HttpServletResponse;

public class Redirect implements HttpResponse {
	private String url;

	public Redirect(String url) {
		this.url = url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	@Override
	public void write(HttpServletResponse resp) throws IOException {
		resp.sendRedirect(url);
	}

}
