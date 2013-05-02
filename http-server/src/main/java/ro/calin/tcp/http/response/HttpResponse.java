package ro.calin.tcp.http.response;

/**
 * @author calin
 */
public class HttpResponse {
    public HttpResponse() {
    }

    public HttpResponse status(HttpStatus status) {
        return this;
    }

    public HttpResponse header(String name, Object value) {
        return this;
    }

    public void body(byte[] html) {
        //To change body of created methods use File | Settings | File Templates.
    }

    public HttpResponse chunck(int len, byte[] data) {
        return this;
    }
}
