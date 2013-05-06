package ro.calin.tcp.http;

import ro.calin.tcp.ProtocolHandler;
import ro.calin.tcp.http.request.HttpVersion;
import ro.calin.tcp.http.request.parser.BadRequestException;
import ro.calin.tcp.http.request.parser.HttpRequestParser;
import ro.calin.tcp.http.request.HttpRequest;
import ro.calin.tcp.http.response.HttpResponse;
import ro.calin.tcp.http.route.HttpRouter;
import ro.calin.tcp.http.route.RequestHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static ro.calin.tcp.http.response.HttpStatus.BAD_REQUEST;
import static ro.calin.tcp.http.response.HttpStatus.NOT_FOUND;

/**
 * @author calin
 *
 * TODO: try to comply with http://www.jmarshall.com/easy/http/#http1.1servers
 */
public class BasicHttpHandler implements ProtocolHandler {
    private HttpRequestParser requestParser;
    private HttpRouter httpRouter;
    private boolean keepAlive;
    private String keepAliveTimeout;

    public BasicHttpHandler(HttpRequestParser requestParser, HttpRouter httpRouter) {
        this.requestParser = requestParser;
        this.httpRouter = httpRouter;
        this.keepAlive = false;
    }

    public BasicHttpHandler(HttpRequestParser requestParser, HttpRouter httpRouter, boolean keepAlive, long keepAliveTimeout) {
        this.requestParser = requestParser;
        this.httpRouter = httpRouter;
        this.keepAlive = keepAlive;
        //TODO: discard connection after max requests in the connection handler
        this.keepAliveTimeout = "timeout=" + keepAliveTimeout + ", max=500";
    }

    @Override
    public boolean handle(InputStream inputStream, OutputStream outputStream) throws IOException {
        HttpRequest httpRequest;
        HttpResponse httpResponse;

        boolean persistConnection = keepAlive;

        try {
            httpRequest = requestParser.parse(inputStream);

            if (persistConnection) {
                if (httpRequest.getVersion() == HttpVersion.HTTP10 || httpRequest.hasHeader("Connection", "close"))
                    persistConnection = false;
            }

            httpResponse = new HttpResponse(outputStream, httpRequest.getVersion());
            RequestHandler requestHandler = httpRouter.findRoute(httpRequest.getMethod(), httpRequest.getUrl());
            if(requestHandler != null) {
                requestHandler.handle(httpRequest, httpResponse);

                if(persistConnection) {
//                    if ("close".equals(httpResponse.getHeaders().get("Connection"))) persistConnection = false;
                }
            } else {
                httpResponse.status(NOT_FOUND);
            }
        } catch (BadRequestException e) {
            httpResponse = new HttpResponse(outputStream, HttpVersion.HTTP11);
            httpResponse.status(BAD_REQUEST);
        }

        if (persistConnection) {
            httpResponse.header("Connection", "Keep-Alive");
            httpResponse.header("Keep-Alive", keepAliveTimeout);
        } else {
            httpResponse.header("Connection", "close");
        }

        return persistConnection;
    }
}
