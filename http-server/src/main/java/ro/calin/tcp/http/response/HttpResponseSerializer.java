package ro.calin.tcp.http.response;

import java.io.OutputStream;

/**
 * @author calin
 */
public interface HttpResponseSerializer {
    /**
     * Writes the response to the output stream.
     * Will close response body input stream if present.
     *
     * @param response
     * @param stream
     */
    void serialize(HttpResponse response, OutputStream stream);
}
