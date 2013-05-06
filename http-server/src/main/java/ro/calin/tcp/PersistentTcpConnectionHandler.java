package ro.calin.tcp;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author cavasilcai
 */
public class PersistentTcpConnectionHandler implements TcpConnectionHandler {
    private static final long MIN_IDLE_TIME = 500; //0.5 sec

    private ProtocolHandler protocolHandler;
    private long maxIdleTime;

    private ExecutorService executor;
    private List<Connection> activeConnections;
    private List<Connection> incomingConnections;
    private volatile boolean running;

    private static class Connection {
        private Socket socket;
        private long lastUsed;
        private boolean beingHandled;

        private Connection(Socket socket, long lastUsed) {
            this.socket = socket;
            this.lastUsed = lastUsed;
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
                if (conn.beingHandled) {
                    System.err.println("Being handled: " + conn);
                    continue;
                }
                try {
                    if (conn.socket.getInputStream().available() > 0) {
                        System.err.println("Handling conn: " + conn);
                        conn.beingHandled = true;
                        executor.submit(new ProtocolHandlerJob(conn));
                    } else if (System.currentTimeMillis() - conn.lastUsed > maxIdleTime) {
                        System.err.println("Killing conn..." + conn);
                        closeConn(conn);
                        it.remove();
                    }
                } catch (Exception e) {
                    System.err.println("Killing conn..." + conn);
                    it.remove();
                    closeConn(conn);

                    e.printStackTrace(); //TODO: log
                }
            }
            System.err.println("Active: " + activeConnections.size());
        }

        private void moveIncomingToActive() {
            synchronized (incomingConnections) {
                activeConnections.addAll(incomingConnections);
                incomingConnections.clear();
            }
        }

        private void closeAll(List<Connection> connections) {
            for (Connection conn : connections) {
                closeConn(conn);
            }
            connections.clear();
        }

        private void closeConn(Connection conn) {
            try {
                conn.socket.close();
            } catch (IOException e) {
            }
        }
    }

    private class ProtocolHandlerJob implements Runnable {
        private Connection conn;

        private ProtocolHandlerJob(Connection conn) {
            this.conn = conn;
        }

        @Override
        public void run() {
            boolean keepConnection = false;

            try {
                keepConnection = protocolHandler.handle(conn.socket.getInputStream(), conn.socket.getOutputStream());
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (keepConnection) conn.lastUsed = System.currentTimeMillis();
            else conn.lastUsed = 0; //make sure it is collected next time
            conn.beingHandled = false;
        }
    }

    public PersistentTcpConnectionHandler(int workers, ProtocolHandler protocolHandler, long maxIdleTime) {
        if(maxIdleTime < MIN_IDLE_TIME) throw new IllegalArgumentException();
        if(workers < 1) throw new IllegalArgumentException();
        if(protocolHandler == null) throw new IllegalArgumentException();

        this.maxIdleTime = maxIdleTime;
        this.protocolHandler = protocolHandler;

        this.activeConnections = new ArrayList<Connection>();
        this.incomingConnections = new ArrayList<Connection>();
        executor = Executors.newFixedThreadPool(workers);
        running = true;

        new Thread(new ConnectionChecker()).start();
    }

    @Override
    public void shutdown() {
        running = false;
        executor.shutdown();
    }

    @Override
    public void handle(final Socket socket) {
        if(!running) return;
        synchronized (incomingConnections) {
            incomingConnections.add(new Connection(socket, Long.MAX_VALUE));
        }
    }
}
