package ro.calin.tcp;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author calin
 */
public class BasicTcpConnectionHandler implements TcpConnectionHandler {
    private ExecutorService executor;
    private ProtocolHandler protocolHandler;
    private volatile boolean running;

    public BasicTcpConnectionHandler(int workers, ProtocolHandler protocolHandler) {
        if(workers < 1) throw new IllegalArgumentException();
        if(protocolHandler == null) throw new IllegalArgumentException();

        this.protocolHandler = protocolHandler;
        executor = Executors.newFixedThreadPool(workers);
        running = true;
    }

    @Override
    public void shutdown() {
        running = false;
        executor.shutdown();
    }

    @Override
    public void handle(final Socket socket) {
        if (running) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        protocolHandler.handle(socket.getInputStream(), socket.getOutputStream());
                        socket.close();
                    } catch (IOException e) {
                        //TODO: handle, log
                    }
                }
            });
        }
    }
}
