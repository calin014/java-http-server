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
public class HttpHandler implements ProtocolHandler {
    private HttpRequestParser requestParser;
    private HttpRouter httpRouter;

    public HttpHandler(HttpRequestParser requestParser, HttpRouter httpRouter) {
        this.requestParser = requestParser;
        this.httpRouter = httpRouter;
    }

    @Override
    public boolean handle(InputStream inputStream, OutputStream outputStream) throws IOException {
        HttpRequest httpRequest;
        HttpResponse httpResponse;

        try {
            httpRequest = requestParser.parse(inputStream);
            httpResponse = new HttpResponse(outputStream, httpRequest.getVersion());
            RequestHandler servler = httpRouter.findRoute(httpRequest.getMethod(), httpRequest.getUrl());
            if(servler != null) {
                servler.handle(httpRequest, httpResponse);
            } else {
                httpResponse.status(NOT_FOUND);
            }
        } catch (BadRequestException e) {
            httpResponse = new HttpResponse(outputStream, HttpVersion.HTTP11);
            httpResponse.status(BAD_REQUEST);
        }

        //TODO: return based on 'Connection: close' header
        return true;
    }
}
