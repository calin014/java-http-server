package ro.calin.fileserver;

import java.io.*;

import ro.calin.tcp.http.Server;
import ro.calin.tcp.http.request.HttpRequest;
import ro.calin.tcp.http.response.HttpResponse;
import ro.calin.tcp.http.response.HttpStatus;
import ro.calin.tcp.http.route.HttpServler;
import static ro.calin.tcp.http.request.HttpMethod.GET;

/**
 * @author cavasilcai
 */
public class FileServer {

    public static void main(String[] args) throws IOException {
        //TODO: get from cmd line
        String root = "/home/cavasilcai/www/";
        final Server server = Server.create()
                .workers(20)
                .port(1234)
                .keepAlive(false)
                .route(GET, ".*", new FileServler(root))
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
}
