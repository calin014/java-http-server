package ro.calin.tcp.http;

import ro.calin.tcp.http.request.HttpMethod;
import ro.calin.tcp.http.route.HttpServler;

/**
 * @author cavasilcai
 */
public class Server {
    private Server() {
    }

    public static Server create() {
        return new Server();
    }

    public Server port(int port) {
        return this;
    }

    public Server route(HttpMethod method, String urlPattern, HttpServler servler) {
        return this;
    }

    public Server keepAlive(boolean flag) {
        return this;
    }

    public Server start() {
        return this;
    }

    public void stop() {
    }
}
