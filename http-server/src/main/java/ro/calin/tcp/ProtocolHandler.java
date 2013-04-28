package ro.calin.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author calin
 */
public interface ProtocolHandler {
    void handle(InputStream inputStream, OutputStream outputStream) throws IOException;
}
