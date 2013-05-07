package ro.calin.tcp;

import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

/**
 * @author calin
 */
public class BasicTcpConnectionHandler implements TcpConnectionHandler {
    final static Logger LOGGER = Logger.getLogger(BasicTcpConnectionHandler.class);

    private ExecutorService executor;
    private ProtocolHandler protocolHandler;
    private volatile boolean running;

    public BasicTcpConnectionHandler(int workers, ProtocolHandler protocolHandler) {
        if(workers < 1) throw new IllegalArgumentException();
        if(protocolHandler == null) throw new IllegalArgumentException();

        this.protocolHandler = protocolHandler;
        executor = Executors.newFixedThreadPool(workers);
        running = true;

        LogMF.info(LOGGER, "Starting connection handling with pool of {0} threads...", workers);
    }

    @Override
    public void shutdown() {
        LOGGER.info("Initiating shutdown process...");
        running = false;
        executor.shutdown();
    }

    @Override
    public void handle(final Socket socket) {
        if (running) {
            LogMF.info(LOGGER, "Registering connection {0} for later processing...", socket);
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        LogMF.info(LOGGER, "Processing connection {0}...", socket);
                        protocolHandler.handle(socket.getInputStream(), socket.getOutputStream());
                        LogMF.info(LOGGER, "Done processing connection {0}...", socket);
                    } catch (Exception e) {
                        LOGGER.fatal("Processing connection failed!", e);
                    }
                    LogMF.info(LOGGER, "Closing connection {0}...", socket);
                    IOUtils.closeQuietly(socket);
                }
            });
        }
    }
}
