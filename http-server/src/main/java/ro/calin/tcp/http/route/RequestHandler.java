package ro.calin.tcp.http.route;

import ro.calin.tcp.http.request.HttpRequest;
import ro.calin.tcp.http.response.HttpResponse;

/**
 * @author calin
 */
public interface RequestHandler {
    HttpResponse handle(HttpRequest request);
}
