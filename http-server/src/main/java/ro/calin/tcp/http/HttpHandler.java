package ro.calin.tcp.http;

import ro.calin.tcp.ProtocolHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author calin
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
        HttpRequest httpRequest = requestParser.parse(inputStream);
        HttpServler servler = httpRouter.findRoute(httpRequest.getMethod(), httpRequest.getUrl());
        HttpResponse httpResponse = new HttpResponse();
        if(servler != null) {
            servler.serve(httpRequest, httpResponse);
        } else {
            httpResponse.status(404);
        }
        responseSerializer.serialize(httpResponse, outputStream);
    }
}
