package ro.calin.tcp.http;

import java.io.InputStream;

/**
 * @author calin
 */
public interface HttpRequestParser {
    HttpRequest parse(InputStream stream);
}
