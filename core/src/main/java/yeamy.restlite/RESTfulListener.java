package yeamy.restlite;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.http.HttpServletRequest;

import static yeamy.restlite.RESTfulRequest.REQUEST;
import static yeamy.restlite.RESTfulRequest.SERVER_NAME;

public class RESTfulListener implements ServletRequestListener {

    public void requestInitialized(ServletRequestEvent sre) {
        ServletRequest req = sre.getServletRequest();
        if (req instanceof HttpServletRequest) {
            createRequest((HttpServletRequest) req);
        }
    }

    public void createRequest(HttpServletRequest httpReq) {
        if ("Upgrade".equals(httpReq.getHeader("Connection"))//
                && "websocket".equals(httpReq.getHeader("Upgrade"))) {
            return;
        }
        try {
            RESTfulRequest restfulReq = HttpRequestFactory.createRequest(httpReq);
            httpReq.setAttribute(REQUEST, restfulReq);
            httpReq.setAttribute(SERVER_NAME, createServerName(restfulReq));
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    public String createServerName(RESTfulRequest restfulReq) {
        return restfulReq.getResource() + ':' + restfulReq.getMethod();
    }
}
