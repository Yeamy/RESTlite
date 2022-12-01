package yeamy.restlite.addition;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import yeamy.restlite.RESTfulRequest;
import yeamy.restlite.HttpResponse;
import yeamy.restlite.RESTfulServlet;

public abstract class SimpleStreamServlet extends RESTfulServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(RESTfulRequest req, HttpServletResponse resp) throws IOException {
        String etag = req.getHeader("If-None-Match");
        long lastModified = req.getDateHeader("If-Modified-Since");
        StreamData data = getStreamData(req);
        if (data == null) {
            doNotFound(resp);
        } else if (data.compare(etag, lastModified)) {
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
        public String getEtag();

        public long getLastModified();

        /**
         * @see StreamResponse
         */
        public HttpResponse getResponse();

        public default boolean compare(String etag, long lastModified) {
            return ETag.compareWeak(etag, this.getEtag())
                    || compareLastModified(lastModified);
        }

        public default boolean compareLastModified(long lastModified) {
            long lmData = getLastModified();
            return lmData > 0 && lastModified >= lmData;
        }
    }

}
