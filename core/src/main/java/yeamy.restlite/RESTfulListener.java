package yeamy.restlite;

import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;

import static yeamy.restlite.RESTfulServlet.REQUEST;

public class RESTfulListener implements ServletRequestListener {

    public void requestInitialized(ServletRequestEvent sre) {
        ServletRequest req = sre.getServletRequest();
        if (req instanceof HttpServletRequest) {
            action((HttpServletRequest) req);
        }
    }

    public void action(HttpServletRequest httpReq) {
        if ("Upgrade".equals(httpReq.getHeader("Connection"))//
                && "websocket".equals(httpReq.getHeader("Upgrade"))) {
            return;
        }
        try {
            httpReq.setAttribute(REQUEST, HttpRequestFactory.createRequest(httpReq));
        } catch (ClassCastException e) {
            e.printStackTrace();
        }

    }
}
