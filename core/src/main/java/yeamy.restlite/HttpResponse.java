package yeamy.restlite;

import yeamy.restlite.addition.*;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

/**
 * @see Forward
 * @see Redirect
 * @see NotModifiedResponse
 * @see VoidResponse
 * @see AbstractHttpResponse
 */
public interface HttpResponse {

	void write(HttpServletResponse resp) throws IOException;
}