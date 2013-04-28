package ro.calin.tcp.http;

import ro.calin.tcp.ProtocolHandler;
import ro.calin.tcp.http.request.parser.BadRequestException;
import ro.calin.tcp.http.request.parser.HttpRequestParser;
import ro.calin.tcp.http.request.HttpRequest;
import ro.calin.tcp.http.response.HttpResponse;
import ro.calin.tcp.http.response.HttpResponseSerializer;
import ro.calin.tcp.http.route.HttpRouter;
import ro.calin.tcp.http.route.HttpServler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author calin
 *
 * TODO: try to comply with http://www.jmarshall.com/easy/http/#http1.1servers
 */
public class HttpHandler implements ProtocolHandler {
    private HttpRequestParser requestParser;
    private HttpRouter httpRouter;
    private HttpResponseSerializer responseSerializer;

    public HttpHandler(HttpRequestParser requestParser, HttpRouter httpRouter, HttpResponseSerializer responseSerializer) {
        this.requestParser = requestParser;
        this.httpRouter = httpRouter;
        this.responseSerializer = responseSerializer;
    }

    @Override
    public void handle(InputStream inputStream, OutputStream outputStream) throws IOException {
        HttpRequest httpRequest;
        HttpResponse httpResponse;

        try {
            httpRequest = requestParser.parse(inputStream);
            HttpServler servler = httpRouter.findRoute(httpRequest.getMethod(), httpRequest.getUrl());
            httpResponse = new HttpResponse();
            if(servler != null) {
                servler.serve(httpRequest, httpResponse);
            } else {
                httpResponse.status(404);
            }
        } catch (BadRequestException e) {
            httpResponse = e.getResponse();
        }

        responseSerializer.serialize(httpResponse, outputStream);
    }
}
