package yeamy.restlite;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Part;
import yeamy.utils.StreamUtils;

import java.io.*;

/**
 * MultiPart File
 *
 * @author Yeamy
 */
public record HttpRequestFile(Part part) implements Serializable {
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
     * read the part as string with charset UTF-8
     *
     * @see #getAsText(String)
     */
    public String getAsText() throws IOException {
        return getAsText("UTF-8");
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
        try (InputStream is = part.getInputStream()) {
            return StreamUtils.write(os, is);
        }
    }

    /**
     * save to local disk (as a file)
     */
    public boolean save(String file) throws IOException {
        try (FileOutputStream os = new FileOutputStream(file);
             InputStream is = part.getInputStream()) {
            return StreamUtils.writeWithoutClose(os, is);
        }
    }

    /**
     * save to local disk (as a file)
     */
    public boolean save(File file) throws IOException {
        try (FileOutputStream os = new FileOutputStream(file);
             InputStream is = part.getInputStream()) {
            return StreamUtils.writeWithoutClose(os, is);
        }
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

}