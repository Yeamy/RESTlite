package yeamy.restlite;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import yeamy.utils.StreamUtils;
import yeamy.utils.TextUtils;

import java.io.IOException;
import java.util.Collection;

public class HttpRequestFactory {

	public static RESTfulRequest createRequest(HttpServletRequest req, boolean isEmbed) {
		RESTfulRequest out = new RESTfulRequest();
		out.insert(req);
		readUri(req, out, isEmbed);
		readBody(req, out);
		return out;
	}

	private static void readUri(HttpServletRequest req, RESTfulRequest out, boolean isEmbed) {
		String uri = req.getRequestURI();
		String[] kv = uri.split("/");
		int skip = isEmbed ? 1 : 2;
		int length = kv.length;
		if (length <= skip) {
			return;
		}
		if ((length - skip) % 2 == 1) {
			out.setResource(kv[length - 1]);
		} else {
			out.setResource(kv[length - 2]);
		}
		for (int i = skip; i < length; i += 2) {
			if (length > i + 1) {
				out.addParameter(kv[i], kv[i + 1]);
			}
		}
		out.dispatch = !out.getResource().equals(kv[skip]);
	}

	public static void readBody(HttpServletRequest req, RESTfulRequest out) {
		String method = req.getMethod();
		switch (method) {
		case "GET":
		case "HEAD":
		case "OPTIONS":
			return;
		}
		String contentType = req.getContentType();
		if (TextUtils.isEmpty(contentType)) {
			return;
		}
		contentType = contentType.toLowerCase();
		if (contentType.contains("multipart/form-data")) {
			readMultiPart(req, out);
		}
	}

	private static void readMultiPart(HttpServletRequest req, RESTfulRequest out) {
		try {
			Collection<Part> parts = req.getParts();
			for (Part part : parts) {
				String name = part.getName();
				String contentType = part.getContentType();
				if (contentType == null) {
					String value = StreamUtils.readString(part.getInputStream(), "UTF-8");
					out.addParameter(name, value);
				} else {
					out.addField(name, new HttpRequestFile(part));
				}
			}
		} catch (IOException | ServletException e) {
			e.printStackTrace();
		}
	}

}
