package yeamy.restlite.addition;

/**
 * Method with processor annotation (such as InjectProvider, ParamProcessor, ...) can throw this
 * Exception to terminate the HTTP request.<br>
 * <b>notice:</b> Can be intercepted by @ERROR
 */
public class ProcessException extends Exception {
    /**
     * The response when Exception
     */
    private final AbstractHttpResponse<?> response;

    /**
     * @param response The response when Exception
     */
    public ProcessException(AbstractHttpResponse<?> response) {
        this.response = response;
    }

    /**
     * @return The response when Exception
     */
    public AbstractHttpResponse<?> getResponse() {
        return response;
    }
}
