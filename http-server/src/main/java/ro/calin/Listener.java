package ro.calin;

/**
 * @author calin
 */
public interface Listener {
    void start() throws IllegalStateException;
    void stop();
    boolean isRunning();
}
