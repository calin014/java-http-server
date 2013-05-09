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

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import static ro.calin.tcp.http.response.HttpStatus.BAD_REQUEST;
import static ro.calin.tcp.http.response.HttpStatus.INTERNAL_SERVER_ERROR;
import static ro.calin.tcp.http.response.HttpStatus.NOT_FOUND;

/**
 * Basic implementation of HTTP/1.1 protocol. Still needs improvement
 * <br/>
 * TODO: at least try to comply with:
 * <a href="http://www.jmarshall.com/easy/http/#http1.1servers">HTTP 1.1 Servers</a>
 *
 * @author calin
 */
public class BasicHttpHandler implements ProtocolHandler {
    final static Logger LOGGER = Logger.getLogger(BasicHttpHandler.class);

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
        //TODO: implement connection discarding after max requests in the connection handler
        this.keepAliveTimeout = "timeout=" + keepAliveTimeout + ", max=500";
    }

    @Override
    public boolean handle(InputStream inputStream, OutputStream outputStream) throws IOException {
        LOGGER.info("Starting HTTP handling...");

        HttpRequest request;
        HttpResponse response;

        boolean persistConnection = keepAlive;

        try {
            request = requestParser.parse(inputStream);
            LogMF.info(LOGGER, "HTTP request was parsed: {0}", request);

            if (persistConnection) {
                if ((request.getVersion() == HttpVersion.HTTP10 && !request.headerValueContains("Connection",
                        "Keep-Alive"))
                        || request.headerValueContains("Connection", "close")) {
                    persistConnection = false;
                    LOGGER.info("Client chooses to CLOSE connection after response is received.");
                }
            }

            RequestHandler requestHandler = httpRouter.findRoute(request.getMethod(), request.getUrl());
            if(requestHandler != null) {
                LogMF.info(LOGGER, "Route found for {0}, {1} -> {2}", request.getMethod(), request.getUrl(), requestHandler.getClass());
                try {
                    response = requestHandler.handle(request);
                } catch (Exception e) {
                    LOGGER.error("There was an error in the request handler.", e);
                    response = HttpResponse.status(INTERNAL_SERVER_ERROR);
                }

                if (response == null) {
                    LOGGER.warn("Request handler returned a NULL response.");
                    response = HttpResponse.status(NOT_FOUND);
                }

                if(persistConnection) {
                    if (response.hasHeader("Connection", "close")) {
                        persistConnection = false;
                        LOGGER.info("Request handler chooses to CLOSE connection after response is sent.");
                    }
                }
            } else {
                LogMF.info(LOGGER, "Route NOT found for {0}, {1}", request.getMethod(), request.getUrl());
                response = HttpResponse.status(NOT_FOUND);
            }
        } catch (BadRequestException e) {
            LOGGER.info("Request could NOT be parsed.");
            response = HttpResponse.status(BAD_REQUEST);
        }

        if (persistConnection) {
            response.header("Connection", "Keep-Alive");
            response.header("Keep-Alive", keepAliveTimeout);
        } else {
            response.header("Connection", "close");
        }

        LogMF.info(LOGGER, "HTTP response was generated: {0}", response);

        responseSerializer.serialize(response, outputStream);

        return persistConnection;
    }
}
