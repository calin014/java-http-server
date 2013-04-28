package ro.calin.tcp;

import ro.calin.Listener;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author calin
 */
public class TcpListener implements Listener, Runnable {

    private TcpConnectionHandler handler;
    private ServerSocket serverSocket;

    private volatile boolean running;
    private boolean started;

    public TcpListener(TcpConnectionHandler handler, int port) throws IOException {
        this.handler = handler;
        this.serverSocket = new ServerSocket(port);
    }

    public void run() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                handler.handle(clientSocket);
            } catch (IOException e) {
                //TODO: log me
            }
        }
    }

    public void start() throws IllegalStateException {
        if(started) throw new IllegalStateException();

        started = true;
        running = true;

        new Thread(this).start();
    }

    public void stop() {
        running = false;

        try {
            serverSocket.close();
        } catch (IOException e) {
            //TODO: log me
        }
    }

    public boolean isRunning() {
        return running;
    }
}
