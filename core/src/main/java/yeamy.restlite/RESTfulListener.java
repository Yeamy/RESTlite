package yeamy.restlite;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.http.HttpServletRequest;

import static yeamy.restlite.RESTfulRequest.REQUEST;

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
