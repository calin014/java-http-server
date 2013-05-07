package ro.calin.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

/**
 * @author calin
 */
public class BasicTcpListener implements TcpListener, Runnable {
    final static Logger LOGGER = Logger.getLogger(BasicTcpListener.class);

    private TcpConnectionHandler handler;
    private ServerSocket serverSocket;

    private volatile boolean running;

    public BasicTcpListener(TcpConnectionHandler handler, int port) throws IOException {
        this.handler = handler;
        this.serverSocket = new ServerSocket(port);

        running = true;
        Thread t = new Thread(this);
        t.setName("Listener");
        t.start();

        LogMF.info(LOGGER, "Listening on port {0}...", port);
    }

    public void run() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                LogMF.info(LOGGER, "Connection accepted: {0}. Begin connection handling...", clientSocket);
                try {
                    handler.handle(clientSocket);
                } catch (Exception e) {
                    LOGGER.error("Connection handling failed!", e);
                }
            } catch (IOException e) {
                if(running) {
                    LOGGER.fatal("Server Socket failed!", e);
                } else {
                    LOGGER.info("Server Socket closed while listening!");
                }
            }
        }
    }

    @Override
    public void shutdown() {
        LOGGER.info("Initiating shutdown process...");
        running = false;
        IOUtils.closeQuietly(serverSocket);
    }
}
