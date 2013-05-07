package ro.calin.tcp;

import org.apache.log4j.BasicConfigurator;
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

    static {
        BasicConfigurator.configure();
    }

    private static class EchoProtocolHandler implements ProtocolHandler {
        @Override
        public boolean handle(InputStream inputStream, OutputStream outputStream) throws IOException {
            PrintWriter out = new PrintWriter(outputStream, true);
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                out.println(inputLine);
                if (inputLine.startsWith(BYE)) break;
            }

            return true;
        }
    }

    private class Client implements Runnable {
        private int id;
        private Socket echoSocket;
        private PrintWriter out;
        private BufferedReader in;
        private Client(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            try {
                connect();

                for(int i = 0; i < 5; i++) {

                    out.println("Hi. I am client " + id);
                    out.println(BYE + " from " + id);

                    if(out.checkError()) {
                        System.out.println("Client " + id + " reconnecting...");
                        closeConnection();
                        connect();
                        out.println("Hi. I am client " + id);
                        out.println(BYE + " from " + id);
                    }

                    String inputLine;
                    //TODO: server might close connection here for some reason
                    while ((inputLine = in.readLine()) != null) {
                        System.out.println(inputLine);
                        if (inputLine.startsWith(BYE)) break;
                    }

                    Thread.sleep((long) (150 + Math.random() * 1000));
                }

                Thread.sleep(2000);

                closeConnection();
            } catch (Exception e) {
                //TODO: we should not fail the first time
                System.err.println("Error for " + id);
                e.printStackTrace();
                Assert.fail();
            }
        }

        private void closeConnection() throws IOException {
            out.close();
            in.close();
            echoSocket.close();
        }

        private void connect() throws IOException {
            echoSocket = new Socket(LOCALHOST, PORT);
            out = new PrintWriter(echoSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(
                    echoSocket.getInputStream()));
        }
    }

    @Before
    public void prepare() throws IOException {
        int workers = 10;

        connectionHandler = new PersistentTcpConnectionHandler(workers, new EchoProtocolHandler(), 1000);
        tcpListener = new BasicTcpListener(connectionHandler, PORT);
    }

    @After
    public void destroy() {
        tcpListener.shutdown();
        connectionHandler.shutdown();
    }

    @Test(timeout = 30000)
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
            Thread.sleep((long) (150 + Math.random() * 150));
        }

        for (Thread t : clients) {
            t.join();
        }

        Assert.assertEquals(0, failedClients.size());
    }
}
