package ro.calin.tcp.http.response;

import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * @author calin
 */
public class HttpResponse {
    private final PrintWriter pw;
    private final OutputStream outputStream;

    public HttpResponse(OutputStream outputStream) {
        this.outputStream = outputStream;
        this.pw = new PrintWriter(outputStream);
    }

    public HttpResponse status(HttpStatus status) {
        return this;
    }

    public HttpResponse header(String name, Object value) {
        return this;
    }

    public void body(byte[] body) {
        //To change body of created methods use File | Settings | File Templates.
    }

    public HttpResponse chunck(int len, byte[] data) {
        return this;
    }
}
