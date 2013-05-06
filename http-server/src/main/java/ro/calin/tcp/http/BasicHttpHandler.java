package ro.calin.tcp.http;

import ro.calin.tcp.ProtocolHandler;
import ro.calin.tcp.http.request.HttpVersion;
import ro.calin.tcp.http.request.parser.BadRequestException;
import ro.calin.tcp.http.request.parser.HttpRequestParser;
import ro.calin.tcp.http.request.HttpRequest;
import ro.calin.tcp.http.response.HttpResponse;
import ro.calin.tcp.http.response.HttpResponseSerializer;
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
    private HttpResponseSerializer responseSerializer;

    private boolean keepAlive;
    private String keepAliveTimeout;

    public BasicHttpHandler(HttpRequestParser requestParser, HttpRouter httpRouter, HttpResponseSerializer responseSerializer) {
        this.requestParser = requestParser;
        this.httpRouter = httpRouter;
        this.responseSerializer = responseSerializer;
        this.keepAlive = false;
    }

    public BasicHttpHandler(HttpRequestParser requestParser, HttpRouter httpRouter, boolean keepAlive, long keepAliveTimeout, HttpResponseSerializer responseSerializer) {
        this.requestParser = requestParser;
        this.httpRouter = httpRouter;
        this.responseSerializer = responseSerializer;

        this.keepAlive = keepAlive;
        //TODO: discard connection after max requests in the connection handler
        this.keepAliveTimeout = "timeout=" + keepAliveTimeout + ", max=500";
    }

    @Override
    public boolean handle(InputStream inputStream, OutputStream outputStream) throws IOException {
        HttpRequest request;
        HttpResponse response;

        boolean persistConnection = keepAlive;

        try {
            request = requestParser.parse(inputStream);

            if (persistConnection) {
                if ((request.getVersion() == HttpVersion.HTTP10 && !request.hasHeader("Connection", "Keep-Alive"))
                        || request.hasHeader("Connection", "close"))
                    persistConnection = false;
            }

            RequestHandler requestHandler = httpRouter.findRoute(request.getMethod(), request.getUrl());
            if(requestHandler != null) {
                response = requestHandler.handle(request);

                if(persistConnection) {
                    if (response.hasHeader("Connection", "close")) persistConnection = false;
                }
            } else {
                response = HttpResponse.status(NOT_FOUND);
            }
        } catch (BadRequestException e) {
            response = HttpResponse.status(BAD_REQUEST);
        }

        if (persistConnection) {
            response.header("Connection", "Keep-Alive");
            response.header("Keep-Alive", keepAliveTimeout);
        } else {
            response.header("Connection", "close");
        }

        responseSerializer.serialize(response, outputStream);

        return persistConnection;
    }
}
