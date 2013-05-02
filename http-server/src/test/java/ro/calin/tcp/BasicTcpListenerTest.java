package ro.calin.tcp;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * @author calin
 */
public class BasicTcpListenerTest {
    public static final String LOCALHOST = "localhost";
    public static final String BYE = "Bye";
    public static final int PORT = 12345;

    private TcpListener tcpListener;
    private TcpConnectionHandler connectionHandler;

    private static class EchoProtocolHandler implements ProtocolHandler {
        @Override
        public void handle(InputStream inputStream, OutputStream outputStream) throws IOException {
            PrintWriter out = new PrintWriter(outputStream, true);
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                out.println(inputLine);
                if (inputLine.startsWith(BYE)) break;
            }

            out.println(Thread.currentThread().getName());

            out.close();
            in.close();
        }
    }

    private class Client implements Runnable {
        private int id;

        private Client(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            Socket echoSocket;
            PrintWriter out;
            BufferedReader in;

            try {
                echoSocket = new Socket(LOCALHOST, PORT);
                out = new PrintWriter(echoSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(
                        echoSocket.getInputStream()));
                out.println("Hi. I am client " + id);
                out.println(BYE + " from " + id);

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println(inputLine);
                }

                out.close();
                in.close();
                echoSocket.close();
            } catch (Exception e) {
                Assert.fail();
            }
        }
    }

    @Before
    public void prepare() throws IOException {
        int workers = 10;

        connectionHandler = new BasicTcpConnectionHandler(workers, new EchoProtocolHandler());
        tcpListener = new BasicTcpListener(connectionHandler, PORT);
    }

    @After
    public void destroy() {
        tcpListener.shutdown();
        connectionHandler.shutdown();
    }

    @Test(timeout = 1000)
    public void basicTest() throws InterruptedException {
        final List<Thread> failedClients = new ArrayList<Thread>();
        List<Thread> clients = new ArrayList<Thread>();
        for (int i = 0; i < 20; i++) {
            final int id = i;
            Thread client = new Thread(new Client(id));
            client.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    failedClients.add(t);
                }
            });
            client.start();
            clients.add(client);
        }

        for (Thread t : clients) {
            t.join();
        }

        Assert.assertEquals(0, failedClients.size());
    }
}
