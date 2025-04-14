package yeamy.restlite.addition;

import yeamy.restlite.RESTfulRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * handle class for cors
 */
public class CorsHandle {
    /**
     * http request
     */
    protected final RESTfulRequest request;
    /**
     * http response
     */
    protected final HttpServletResponse response;
    private boolean intercept = false;

    /**
     * @param request http request from web filter
     * @param response http response from web filter
     */
    public CorsHandle(RESTfulRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    /**
     * @return get http request
     */
    public RESTfulRequest getRequest() {
        return request;
    }

    /**
     * @return get http request
     */
    public HttpServletRequest getServletRequest() {
        return request.getRequest();
    }

    /**
     * @return get http response
     */
    public HttpServletResponse getResponse() {
        return response;
    }

    /**
     * @return get request header Origin
     */
    public String getRequestOrigin() {
        return request.getHeader("Origin");
    }

    /**
     * @return get request header Access-Control-Request-Methods
     */
    public String[] getRequestMethods() {
        return request.getHeaderAsArray("Access-Control-Request-Methods");
    }

    /**
     * @return get request header Access-Control-Request-Headers
     */
    public String[] getRequestHeaders() {
        return request.getHeaderAsArray("Access-Control-Request-Headers");
    }

    /**
     * @param origin set response header Access-Control-Allow-Origin
     */
    public void setAllowOrigin(String origin) {
        response.setHeader("Access-Control-Allow-Origin", origin);
    }

    /**
     * @param methods set response header Access-Control-Allow-Methods
     */
    public void setAllowMethods(String methods) {
        response.setHeader("Access-Control-Allow-Methods", methods);
    }

    /**
     * @param method set response header Access-Control-Allow-Methods
     */
    public void setAllowMethods(String... method) {
        response.setHeader("Access-Control-Allow-Methods", String.join(", ", method));
    }

    /**
     * @param headers set response header Access-Control-Allow-Headers
     */
    public void setAllowHeaders(String headers) {
        response.setHeader("Access-Control-Allow-Headers", headers);
    }

    /**
     * @param time set response header Access-Control-Max-Age
     */
    public void setAllowMaxAge(long time) {
        response.setHeader("Access-Control-Max-Age", String.valueOf(time));
    }

    /**
     * @param vary add response header Vary
     */
    public void addVary(String vary) {
        response.addHeader("Vary", vary);
    }

    /**
     * @param intercept intercept and other filter cannot receive this request
     */
    public void setIntercept(boolean intercept) {
        this.intercept = intercept;
    }

    /**
     * @return intercept request
     */
    public boolean isIntercept() {
        return intercept;
    }
}
