package ro.calin.webapp;

import ro.calin.tcp.http.request.HttpRequest;
import ro.calin.tcp.http.response.HttpResponse;
import ro.calin.tcp.http.response.HttpStatus;
import ro.calin.tcp.http.route.RequestHandler;

import java.io.ByteArrayInputStream;

import static ro.calin.tcp.http.response.HttpStatus.*;

/**
 * @author cavasilcai
 */
public class ApiRequestHandler implements RequestHandler {
    @Override
    public HttpResponse handle(HttpRequest request) {
        final HttpStatus[] statuses = {OK, CREATED, BAD_REQUEST};
        final String body =
                "{\"message\": \"Hi " + request.paramValue("id") +
                ". I like to send random status codes.\"}";
        return HttpResponse
                .status(statuses[((int) (Math.random() * statuses.length))])
                .header("Content-Type", "application/json")
                .body(new ByteArrayInputStream(body.getBytes()));
    }
}
