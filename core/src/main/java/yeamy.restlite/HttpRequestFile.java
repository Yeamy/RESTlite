package yeamy.restlite;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import yeamy.utils.StreamUtils;

/**
 * MultiPart
 * 
 * @author Yeamy
 */
public class HttpRequestFile implements Serializable {
	private static final long serialVersionUID = -3268287626476820927L;
	private final Part part;

	public HttpRequestFile(Part part) {
		this.part = part;
	}

	public String contentType() {
		return this.part.getContentType();
	}

	/** parameter name */
	public String name() {
		return this.part.getName();
	}

	public String fileName() {
		return this.part.getSubmittedFileName();
	}

	/** the extension name of fileName */
	public String extension() {
		String name = fileName();
		if (name == null) {
			return "";
		}
		int beginIndex = name.lastIndexOf('.');
		if (beginIndex == -1) {
			return "";
		}
		return name.substring(beginIndex);
	}

	public String getAsText() throws IOException {
		return getAsText("utf-8");
	}

	public String getAsText(String charset) throws IOException {
		return StreamUtils.readString(part.getInputStream(), charset);
	}

	public byte[] getAsByte() throws IOException {
		return StreamUtils.readByte(part.getInputStream());
	}

	public InputStream get() throws IOException {
		return part.getInputStream();
	}

	/** save to local disk (as a file) */
	public boolean save(String file) throws IOException {
		FileOutputStream os = new FileOutputStream(file);
		InputStream is = part.getInputStream();
		return StreamUtils.write(os, is);
	}

	public static File getProjectPath(RESTfulRequest hq) {
		HttpServletRequest req = hq.getRequest();
		// /.../webapps/[projectName]/
		return new File(req.getServletContext().getRealPath(""));
	}

}