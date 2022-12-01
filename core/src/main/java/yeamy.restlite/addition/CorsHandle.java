package yeamy.restlite.addition;

import yeamy.restlite.RESTfulRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CorsHandle {
    protected final RESTfulRequest request;
    protected final HttpServletResponse response;
    private boolean intercept = false;

    public CorsHandle(RESTfulRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    public RESTfulRequest getRequest() {
        return request;
    }

    public HttpServletRequest getServletRequest() {
        return request.getRequest();
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public String getRequestOrigin() {
        return request.getHeader("Origin");
    }

    public String[] getRequestMethods() {
        return request.getHeaderAsArray("Access-Control-Request-Methods");
    }

    public String[] getRequestHeaders() {
        return request.getHeaderAsArray("Access-Control-Request-Headers");
    }

    public void setAllowOrigin(String origin) {
        response.setHeader("Access-Control-Allow-Origin", origin);
    }

    public void setAllowMethods(String methods) {
        response.setHeader("Access-Control-Allow-Methods", methods);
    }

    public void setAllowMethods(String... method) {
        response.setHeader("Access-Control-Allow-Methods", String.join(", ", method));
    }

    public void setAllowHeaders(String headers) {
        response.setHeader("Access-Control-Allow-Headers", headers);
    }

    public void setAllowMaxAge(long time) {
        response.setHeader("Access-Control-Max-Age", String.valueOf(time));
    }

    public void addVary(String vary) {
        response.addHeader("Vary", vary);
    }

    public void setIntercept(boolean intercept) {
        this.intercept = intercept;
    }

    public boolean isIntercept() {
        return intercept;
    }
}
