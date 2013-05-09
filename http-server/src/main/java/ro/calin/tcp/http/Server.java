package ro.calin.tcp.http;

import ro.calin.tcp.*;
import ro.calin.tcp.http.request.HttpMethod;
import ro.calin.tcp.http.request.parser.BasicHttpRequestParser;
import ro.calin.tcp.http.request.parser.HttpRequestParser;
import ro.calin.tcp.http.response.BasicHttpResponseSerializer;
import ro.calin.tcp.http.response.HttpResponseSerializer;
import ro.calin.tcp.http.route.BasicHttpRouter;
import ro.calin.tcp.http.route.HttpRouter;
import ro.calin.tcp.http.route.RequestHandler;

import java.io.IOException;

/**
 * Component used to assemble server components
 * and create a HTTP server based on different options.
 *
 * @author cavasilcai
 */
public class Server {
    public static final long MAX_IDLE_TIME = 15000; //15 sec

    private TcpListener tcpListener;
    private TcpConnectionHandler tcpConnectionHandler;
    private ProtocolHandler protocolHandler;
    private HttpRequestParser httpRequestParser;
    private HttpRouter httpRouter;
    private int port = 80;
    private int workers = 10;
    private boolean keepAlive = false;
    private boolean started = false;

    private Server() {
        httpRouter = new BasicHttpRouter();
    }

    public static Server create() {
        return new Server();
    }

    public Server port(int port) {
        if (started) throw new IllegalStateException("Already started.");
        this.port = port;
        return this;
    }

    public Server workers(int workers) {
        if (started) throw new IllegalStateException("Already started.");
        this.workers = workers;
        return this;
    }

    public Server route(HttpMethod method, String urlPattern, RequestHandler servler) {
        httpRouter.addRoute(method, urlPattern, servler);
        return this;
    }

    public Server keepAlive(boolean keepAlive) {
        if (started) throw new IllegalStateException("Already started.");
        this.keepAlive = keepAlive;
        return this;
    }

    public Server start() throws IOException {
        if (started) throw new IllegalStateException("Already started.");
        started = true;

        httpRequestParser = new BasicHttpRequestParser();
        HttpResponseSerializer responseSerializer = new BasicHttpResponseSerializer();
        protocolHandler = keepAlive ?
                new BasicHttpHandler(httpRequestParser, httpRouter, keepAlive, MAX_IDLE_TIME, responseSerializer) :
                new BasicHttpHandler(httpRequestParser, httpRouter, responseSerializer);
        tcpConnectionHandler = keepAlive ?
                new PersistentTcpConnectionHandler(workers, protocolHandler, MAX_IDLE_TIME) :
                new BasicTcpConnectionHandler(workers, protocolHandler);
        tcpListener = new BasicTcpListener(tcpConnectionHandler, port);
        return this;
    }

    public void stop() {
        if (!started) throw new IllegalStateException("Already stopped.");
        tcpListener.shutdown();
        tcpConnectionHandler.shutdown();
        started = false;
    }
}
