package ro.calin.tcp.http;

import java.io.OutputStream;

/**
 * @author calin
 */
public interface HttpResponseSerializer {
    void serialize(HttpResponse response, OutputStream stream);
}
