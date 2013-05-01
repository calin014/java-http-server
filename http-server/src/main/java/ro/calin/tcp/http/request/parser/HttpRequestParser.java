package ro.calin.tcp.http.request.parser;

import ro.calin.tcp.http.request.HttpRequest;

import java.io.InputStream;

/**
 * @author calin
 */
public interface HttpRequestParser {
    HttpRequest parse(InputStream stream) throws BadRequestException;
}
