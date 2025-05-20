package yeamy.restlite.addition;

import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.List;
import java.util.Random;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

import yeamy.restlite.utils.StreamUtils;
import yeamy.restlite.utils.TextUtils;

public class StreamResponse extends AbstractHttpResponse<InputStream> {
    protected ContentDisposition download = null;
    protected List<Range> requestRanges;
    protected String filename;
    protected int totalLength;
    protected long lastModified;
    private String boundary;
    private String eTag;

    public StreamResponse(InputStream is) throws IOException {
        super(is);
        this.totalLength = is.available();
    }

    public StreamResponse(File file) throws IOException {
        super(Files.newInputStream(file.toPath()));
        this.totalLength = getData().available();
        this.lastModified = file.lastModified();
        this.eTag = ETag.fileETag(file);
        this.download = ContentDisposition.attachment;
        setFilename(file.getName());
    }

    public StreamResponse(byte[] bts) {
        super(new ByteArrayInputStream(bts));
        this.totalLength = bts.length;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public void setETag(String eTag) {
        this.eTag = eTag;
    }

    public void setRequestRange(String range) {
        requestRanges = Range.parse(range, totalLength);
    }

    public void setDownload(ContentDisposition download) {
        this.download = download;
    }

    public ContentDisposition getDownload() {
        return download;
    }

    public String getContentDisposition() throws UnsupportedEncodingException {
        if (download == null && filename == null) {
            return null;
        }
        StringBuilder b = new StringBuilder();
        if (download != null) {
            b.append(download).append("; ");
        }
        if (filename != null) {
            b.append("filename=").append(URLEncoder.encode(filename, getCharset()));
        }
        return b.toString();
    }

    public void setFilename(String filename) {
        this.filename = filename;
        if (TextUtils.isEmpty(contentType())) {
            try {
                setContentType(Files.probeContentType(new File(filename).toPath()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getFilename() {
        return filename;
    }

    @Override
    public void writeContent(HttpServletResponse resp) throws IOException {
        if (eTag != null) {
            resp.setHeader("ETag", eTag);
        }
        if (lastModified > 0) {
            resp.setDateHeader("Last-Modified", lastModified);
        }
        if (requestRanges == null) {
            writeAll(resp);
        } else if (requestRanges.size() == 1) {
            writeRange(resp, requestRanges.get(0));
        } else {
            writeMultiPart(resp, requestRanges);
        }
    }

    public String getMultiPartBoundary() {
        if (boundary == null) {
            Random random = new Random();
            char[] cc = new char[6];
            for (int i = 0; i < cc.length; i++) {
                int x = random.nextInt(52);
                cc[i] = x >= 26 ? (char) (x - 26 + 'A') : (char) ('a' + x);
            }
            boundary = new String(cc);
        }
        return boundary;
    }

    public void writeAll(HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setHeader("Accept-Ranges", "bytes");
        String contentDisposition = getContentDisposition();
        if (contentDisposition != null) {
            resp.setHeader("Content-Disposition", contentDisposition);
        }
        try (ServletOutputStream os = resp.getOutputStream();
             InputStream is = getData()) {
            if (is != null) {
                resp.setContentLength(totalLength);
                StreamUtils.write(os, is);
            } else {
                throw new NullPointerException("Null Stream to serialize");
            }
        }
    }

    public void writeRange(HttpServletResponse resp, Range r) throws IOException {
        resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        resp.setHeader("Content-Range", r.contentRange());
        long len = r.length();
        resp.setContentLengthLong(len);
        try (ServletOutputStream os = resp.getOutputStream(); //
             InputStream is = getData()) {
            StreamUtils.write(os, is, r.first, len);
        }
    }

    public void writeMultiPart(HttpServletResponse resp, List<Range> rs) throws IOException {
        resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        final String boundary = getMultiPartBoundary();
        resp.setContentType("multipart/byteranges; boundary=" + boundary);
        try (ServletOutputStream os = resp.getOutputStream(); //
             InputStream is = getData()) {
            for (Range r : rs) {
                os.print(r.beginningOfMultiPart(boundary, contentType()));
                is.reset();
                StreamUtils.writeWithoutClose(os, is, r.first, r.length());
            }
            os.print(Range.endOfMultiPart(boundary));
        }
    }

    public enum ContentDisposition {
        /** download */
        attachment,
        /** show in web browser */
        inline
    }

}