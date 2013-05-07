package ro.calin.tcp;

/**
 * An implementation will start listening for tcp connections upon creation.
 * Calling {@link ro.calin.tcp.TcpListener#shutdown()} will stop the listening process.
 *
 * @author calin
 */
public interface TcpListener {
    void shutdown();
}
