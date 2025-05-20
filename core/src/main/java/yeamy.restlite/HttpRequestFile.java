package yeamy.restlite;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Part;
import yeamy.restlite.utils.StreamUtils;
import yeamy.restlite.utils.TextUtils;

import java.io.*;

/**
 * MultiPart File
 *
 * @author Yeamy
 */
public record HttpRequestFile(Part part, String bodyCharset) implements Serializable {
    @Serial
    private static final long serialVersionUID = -3268287626476820927L;

    /**
     * The content type passed by the browser or null if not defined.
     */
    public String contentType() {
        return this.part.getContentType();
    }

    /**
     * parameter name
     */
    public String name() {
        return this.part.getName();
    }

    /**
     * the submitted file name or null.
     *
     * @see Part#getSubmittedFileName()
     */
    public String fileName() {
        return this.part.getSubmittedFileName();
    }

    /**
     * the extension name of fileName
     */
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

    /**
     * read the part as string
     *
     * @see #getAsText(String)
     */
    public String getAsText() throws IOException {
        String cs = charset();
        return getAsText(TextUtils.isNotEmpty(cs) ? cs : bodyCharset);
    }

    /**
     * read the part as string with given charset
     */
    public String getAsText(String charset) throws IOException {
        return StreamUtils.readString(part.getInputStream(), charset);
    }

    /**
     * read the part as byte array
     */
    public byte[] getAsByte() throws IOException {
        return StreamUtils.readByte(part.getInputStream());
    }

    /**
     * get the InputStream
     */
    public InputStream get() throws IOException {
        return part.getInputStream();
    }

    /**
     * save to OutputStream
     */
    public boolean save(OutputStream os) throws IOException {
        return StreamUtils.write(os, part.getInputStream());
    }

    /**
     * save to local disk (as a file)
     */
    public void save(String file) throws IOException {
        StreamUtils.writeWithoutClose(new FileOutputStream(file), part.getInputStream());
    }

    /**
     * save to local disk (as a file)
     */
    public void save(File file) throws IOException {
        StreamUtils.writeWithoutClose(new FileOutputStream(file), part.getInputStream());
    }

    /**
     * Returns a String containing the real path for a given virtual path.
     *
     * @see ServletContext#getRealPath(String)
     */
    public static File getProjectPath(RESTfulRequest hq) {
        // /.../webapps/[projectName]/
        return new File(hq.getRequest().getServletContext().getRealPath(""));
    }

    public String charset() {
        String contentType = contentType();
        if (TextUtils.isNotEmpty(contentType)) {
            int i = contentType.indexOf("charset");
            if (i > 0) {
                return contentType.substring(contentType.indexOf('=', i));
            }
        }
        return null;
    }
}