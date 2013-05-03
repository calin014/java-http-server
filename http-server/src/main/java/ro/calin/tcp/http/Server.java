package ro.calin.tcp.http;

import java.io.IOException;
import ro.calin.tcp.BasicTcpConnectionHandler;
import ro.calin.tcp.BasicTcpListener;
import ro.calin.tcp.KeepAliveTcpConnectionHandler;
import ro.calin.tcp.ProtocolHandler;
import ro.calin.tcp.TcpConnectionHandler;
import ro.calin.tcp.TcpListener;
import ro.calin.tcp.http.request.HttpMethod;
import ro.calin.tcp.http.request.parser.BasicHttpRequestParser;
import ro.calin.tcp.http.request.parser.HttpRequestParser;
import ro.calin.tcp.http.route.BasicHttpRouter;
import ro.calin.tcp.http.route.HttpRouter;
import ro.calin.tcp.http.route.HttpServler;
import sun.plugin.dom.exception.InvalidStateException;

/**
 * @author cavasilcai
 */
public class Server {
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
        if (started) throw new InvalidStateException("Already started.");
        this.port = port;
        return this;
    }

    public Server workers(int workers) {
        if (started) throw new InvalidStateException("Already started.");
        this.workers = workers;
        return this;
    }

    public Server route(HttpMethod method, String urlPattern, HttpServler servler) {
        httpRouter.addRoute(method, urlPattern, servler);
        return this;
    }

    public Server keepAlive(boolean keepAlive) {
        if (started) throw new InvalidStateException("Already started.");
        this.keepAlive = keepAlive;
        return this;
    }

    public Server start() throws IOException {
        if (started) throw new InvalidStateException("Already started.");
        started = true;

        httpRequestParser = new BasicHttpRequestParser();
        protocolHandler = new HttpHandler(httpRequestParser, httpRouter);
        tcpConnectionHandler = keepAlive?
                new KeepAliveTcpConnectionHandler() :
                new BasicTcpConnectionHandler(workers, protocolHandler);
        tcpListener = new BasicTcpListener(tcpConnectionHandler, port);
        return this;
    }

    public void stop() {
        if (!started) throw new InvalidStateException("Already stopped.");
        tcpListener.shutdown();
        tcpConnectionHandler.shutdown();
        started = false;
    }
}
