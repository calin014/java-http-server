package ro.calin.tcp.http.response;

import java.io.OutputStream;

/**
 * @author calin
 */
public interface HttpResponseSerializer {
    void serialize(HttpResponse response, OutputStream stream);
}
