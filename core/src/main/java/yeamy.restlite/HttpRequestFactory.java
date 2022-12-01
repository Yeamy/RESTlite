package yeamy.restlite;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import yeamy.utils.StreamUtils;
import yeamy.utils.TextUtils;

public class HttpRequestFactory {

	public static RESTfulRequest createRequest(HttpServletRequest req) {
		RESTfulRequest out = new RESTfulRequest();
		out.insert(req);
		readUri(req, out);
		readBody(req, out);
		return out;
	}

	private static void readUri(HttpServletRequest req, RESTfulRequest out) {
		String uri = req.getRequestURI();
		String[] kv = uri.split("/");
		String[] kv2 = new String[kv.length - 1];
		System.arraycopy(kv, 1, kv2, 0, kv2.length);
		kv2 = kv;
		int skip = 2;
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
