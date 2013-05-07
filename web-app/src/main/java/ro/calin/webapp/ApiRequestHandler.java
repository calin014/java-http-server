package ro.calin.webapp;

import ro.calin.tcp.http.request.HttpRequest;
import ro.calin.tcp.http.response.HttpResponse;
import ro.calin.tcp.http.response.HttpStatus;
import ro.calin.tcp.http.route.RequestHandler;

/**
 * @author cavasilcai
 */
public class ApiRequestHandler implements RequestHandler {
    @Override
    public HttpResponse handle(HttpRequest request) {
        final HttpStatus[] statuses = HttpStatus.values();
        return HttpResponse.status(statuses[((int) (Math.random() * statuses.length))]);
    }
}
