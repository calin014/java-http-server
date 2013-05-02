package ro.calin.fileserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import ro.calin.tcp.http.Server;
import ro.calin.tcp.http.request.HttpRequest;
import ro.calin.tcp.http.response.HttpResponse;
import ro.calin.tcp.http.route.HttpServler;
import static ro.calin.tcp.http.request.HttpMethod.GET;

/**
 * @author cavasilcai
 */
public class FileServer {

    public static void main(String[] args) throws IOException {
        final Server server = Server
                .create()
                .port(3000) //TODO: get from cmd line
                .route(GET, ".*", new FileServler())
                .keepAlive(true)
                .start();

        waitForStop();
        server.stop();
    }

    private static void waitForStop() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String line;

        while ((line = in.readLine()) != null) {
            if ("stop".equals(line))
                break;
        }
    }

    private static class FileServler implements HttpServler {

        @Override
        public void serve(HttpRequest request, HttpResponse response) {
            // To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
