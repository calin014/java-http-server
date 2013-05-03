package ro.calin.tcp.http.response;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import ro.calin.tcp.http.request.HttpVersion;

/**
 * @author calin
 */
public class HttpResponse {
    public static final String CRLF = "\r\n";
    private final PrintWriter writer;
    private final OutputStream outputStream;
    private HttpVersion version;

    public HttpResponse(OutputStream outputStream, HttpVersion version) {
        this.outputStream = outputStream;
        this.version = version;
        this.writer = new PrintWriter(outputStream);
    }

    public HttpResponse status(HttpStatus status) {
        writer.print(version.getVersion() + " " + status.getStatus() + CRLF);
        return this;
    }

    public HttpResponse header(String name, Object value) {
        writer.print(name + ": " + value.toString() + CRLF);
        return this;
    }

    public void body(byte[] body) {
        writer.write(CRLF);
        writer.flush();
        try {
            outputStream.write(body);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
