package yeamy.restlite.httpclient;

public class ClientResponse<T> {
    public final int code;
    public final String contentType;
    public final String charset;
    public final T content;

    public ClientResponse(int code, String contentType, String charset, T content) {
        this.code = code;
        this.contentType = contentType;
        this.charset = charset;
        this.content = content;
    }
}
