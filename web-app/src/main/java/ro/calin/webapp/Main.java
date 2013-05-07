package ro.calin.webapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.log4j.BasicConfigurator;
import ro.calin.tcp.http.Server;
import static ro.calin.tcp.http.request.HttpMethod.GET;
import static ro.calin.tcp.http.request.HttpMethod.POST;

/**
 * @author cavasilcai
 */
public class Main {
    public static void main(String[] args) throws IOException {
        BasicConfigurator.configure();

        final Server server = Server.create()
                                    .workers(5)
                                    .port(12345)
                                    .keepAlive(true)
                                    .route(GET, "/webapp", new WebappRequestHandler())
                                    .route(POST, "/api(/.*)*", new ApiRequestHandler())
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
