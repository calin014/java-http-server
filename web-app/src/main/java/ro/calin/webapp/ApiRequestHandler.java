package ro.calin.webapp;

import java.io.ByteArrayInputStream;
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
        final String body = "{'data':'Server says: Hi" + request.paramValue("id") + "'}";
        return HttpResponse
                .status(statuses[((int) (Math.random() * statuses.length))])
                .body(new ByteArrayInputStream(body.getBytes()));
    }
}
