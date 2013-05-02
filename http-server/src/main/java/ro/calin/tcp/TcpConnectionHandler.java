package ro.calin.tcp;

import java.net.Socket;

/**
 * @author cavasilcai
 */
public interface TcpConnectionHandler {
    void shutdown();
    void handle(Socket socket);
}
