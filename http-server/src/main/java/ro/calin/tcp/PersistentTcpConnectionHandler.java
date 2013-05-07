package ro.calin.tcp;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

/**
 * @author cavasilcai
 */
public class PersistentTcpConnectionHandler implements TcpConnectionHandler {
    final static Logger LOGGER = Logger.getLogger(PersistentTcpConnectionHandler.class);

    private static final long MIN_ALLOWED_MAX_IDLE_TIME = 500; //0.5 sec
    private static final long MAX_PROCESSING_TIME = 10000; //10 sec

    private ProtocolHandler protocolHandler;
    private long maxIdleTime;

    private ExecutorService executor;
    private List<Connection> activeConnections;
    private List<Connection> incomingConnections;
    private volatile boolean running;

    private static class Connection {
        private Socket socket;
        private volatile long lastTouched;
        private volatile boolean beingHandled;

        private Connection(Socket socket, long lastTouched) {
            this.socket = socket;
            this.lastTouched = lastTouched;
        }

        @Override
        public String toString() {
            return "Connection{" +
                    "socket=" + socket +
                    ", lastTouched=" + lastTouched +
                    ", beingHandled=" + beingHandled +
                    '}';
        }
    }

    private class ConnectionChecker implements Runnable {
        @Override
        public void run() {
            while (running) {
                moveIncomingToActive();
                checkActiveConnections();
                sleep();
            }

            closeAll(activeConnections);
            closeAll(incomingConnections);
        }

        private void sleep() {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
            }
        }

        private void checkActiveConnections() {
            Iterator<Connection> it = activeConnections.iterator();
            while (it.hasNext()) {
                Connection conn = it.next();
                try {
                    if (conn.beingHandled && System.currentTimeMillis() - conn.lastTouched > MAX_PROCESSING_TIME) {
                        LogMF.warn(LOGGER, "Closing connection {0} because protocol is taking too long to respond or " +
                                "handling job not started yet.", conn);
                        IOUtils.closeQuietly(conn.socket);
                        it.remove();
                        conn.beingHandled = false;
                    } else {
                        if (System.currentTimeMillis() - conn.lastTouched > maxIdleTime) {
                            LogMF.info(LOGGER, "Closing connection {0} because it is inactive for more then {1} ms.", conn, maxIdleTime);
                            IOUtils.closeQuietly(conn.socket);
                            it.remove();
                        } else if (conn.socket.getInputStream().available() > 0) {
                            LogMF.info(LOGGER, "Input detected for connection {0}, schedule for protocol processing...", conn);
                            conn.beingHandled = true;
                            conn.lastTouched = System.currentTimeMillis();
                            executor.submit(new ProtocolHandlerJob(conn));
                        }
                    }
                } catch (Throwable e) {
                    LOGGER.fatal("Checking active connection " + conn + " failed!", e);
                }
            }
        }

        private void moveIncomingToActive() {
            synchronized (incomingConnections) {
                if(incomingConnections.size() > 0) {
                    LogMF.info(LOGGER, "Making active {0} incoming connections...", incomingConnections.size());
                    activeConnections.addAll(incomingConnections);
                    incomingConnections.clear();
                }
            }
        }

        private void closeAll(List<Connection> connections) {
            for (Connection conn : connections) {
                IOUtils.closeQuietly(conn.socket);
            }
            connections.clear();
        }
    }

    private class ProtocolHandlerJob implements Runnable {
        private Connection conn;

        private ProtocolHandlerJob(Connection conn) {
            this.conn = conn;
        }

        @Override
        public void run() {
            if (!conn.beingHandled) {
                LogMF.warn(LOGGER, "Connection {0} processing has been aborted!", conn);
                return;
            }

            boolean keepConnection = false;
            try {
                LogMF.info(LOGGER, "Processing connection {0}...", conn);
                keepConnection = protocolHandler.handle(conn.socket.getInputStream(), conn.socket.getOutputStream());
                LogMF.info(LOGGER, "Done processing connection {0}...", conn);

                if(keepConnection) {
                    LogMF.info(LOGGER, "Protocol decided to keep connection {0} ALIVE.", conn);
                } else {
                    LogMF.info(LOGGER, "Protocol decided to END connection {0}.", conn);
                }
            } catch (Exception e) {
                LOGGER.fatal("Processing connection failed!", e);
            }

            if (keepConnection) conn.lastTouched = System.currentTimeMillis();
            else conn.lastTouched = 0; //make sure it is closed at next iteration
            conn.beingHandled = false;
        }
    }

    public PersistentTcpConnectionHandler(int workers, ProtocolHandler protocolHandler, long maxIdleTime) {
        if(maxIdleTime < MIN_ALLOWED_MAX_IDLE_TIME) throw new IllegalArgumentException();
        if(workers < 1) throw new IllegalArgumentException();
        if(protocolHandler == null) throw new IllegalArgumentException();

        this.maxIdleTime = maxIdleTime;
        this.protocolHandler = protocolHandler;

        this.activeConnections = new ArrayList<Connection>();
        this.incomingConnections = new ArrayList<Connection>();
        executor = Executors.newFixedThreadPool(workers);
        running = true;

        Thread t = new Thread(new ConnectionChecker());
        t.setName("CONNECTION CHECKER");
        t.start();

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
        if(!running) return;

        LogMF.info(LOGGER, "Registering connection {0} for later processing...", socket);
        synchronized (incomingConnections) {
            incomingConnections.add(new Connection(socket, System.currentTimeMillis()));
        }
    }
}
