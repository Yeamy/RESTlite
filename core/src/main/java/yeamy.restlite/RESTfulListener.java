package yeamy.restlite;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.http.HttpServletRequest;

import static yeamy.restlite.RESTfulRequest.REQUEST;

public abstract class RESTfulListener implements ServletRequestListener {

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        ServletRequest req = sre.getServletRequest();
        if (req instanceof HttpServletRequest httpReq) {
            createRequest(httpReq);
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
            restfulReq.setServiceName(createServerName(restfulReq));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String createServerName(RESTfulRequest restfulReq) {
        return restfulReq.getResource() + ':' + restfulReq.getMethod();
    }
}
