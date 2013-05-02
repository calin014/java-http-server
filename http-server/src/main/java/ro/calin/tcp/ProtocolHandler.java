package ro.calin.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author calin
 */
public interface ProtocolHandler {
    /**
     * @param inputStream
     * @param outputStream
     * @return true if the system should not close the channel, false otherwise
     * @throws IOException
     */
    boolean handle(InputStream inputStream, OutputStream outputStream) throws IOException;
}
