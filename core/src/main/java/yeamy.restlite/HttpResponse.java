package yeamy.restlite;

import yeamy.restlite.addition.*;

import java.io.IOException;

import jakarta.servlet.http.HttpServletResponse;

/**
 * @see Forward
 * @see Redirect
 * @see NotModifiedResponse
 * @see VoidResponse
 * @see AbstractHttpResponse
 */
public interface HttpResponse {

    /**
     * Serialize response: set header, cookies, content-type, write output stream
     *
     * @param resp the http servlet response
     * @throws IOException If an input or output exception occurs
     */
    void write(HttpServletResponse resp) throws IOException;
}