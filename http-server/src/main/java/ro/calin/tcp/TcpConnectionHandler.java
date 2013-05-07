package ro.calin.tcp;

import java.net.Socket;

/**
 * An implementation will handle TCP connections.
 * Calling {@link ro.calin.tcp.TcpConnectionHandler#shutdown()} should stop any async processing.
 *
 * @author cavasilcai
 */
public interface TcpConnectionHandler {
    void shutdown();
    void handle(Socket socket);
}
