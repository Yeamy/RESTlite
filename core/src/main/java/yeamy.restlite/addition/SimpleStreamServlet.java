package yeamy.restlite.addition;

import java.io.IOException;

import jakarta.servlet.http.HttpServletResponse;

import yeamy.restlite.RESTfulRequest;
import yeamy.restlite.HttpResponse;
import yeamy.restlite.RESTfulServlet;

public abstract class SimpleStreamServlet extends RESTfulServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(RESTfulRequest req, HttpServletResponse resp) throws IOException {
        String eTag = req.getHeader("If-None-Match");
        long lastModified = req.getDateHeader("If-Modified-Since");
        StreamData data = getStreamData(req);
        if (data == null) {
            doNotFound(resp);
        } else if (data.compare(eTag, lastModified)) {
            new NotModifiedResponse().write(resp);
        } else {
            data.getResponse().write(resp);
        }
    }

    protected void doNotFound(HttpServletResponse resp) throws IOException {
        new VoidResponse(HttpServletResponse.SC_NOT_FOUND).write(resp);
    }

    protected abstract StreamData getStreamData(RESTfulRequest req);

    public interface StreamData {

        /**
         * @see ETag
         */
        String getETag();

        long getLastModified();

        /**
         * @see StreamResponse
         */
        HttpResponse getResponse();

        default boolean compare(String eTag, long lastModified) {
            return ETag.compareWeak(eTag, this.getETag())
                    || compareLastModified(lastModified);
        }

        default boolean compareLastModified(long lastModified) {
            long lmData = getLastModified();
            return lmData > 0 && lastModified >= lmData;
        }
    }

}
