package ro.calin.webapp;

import ro.calin.tcp.http.request.HttpRequest;
import ro.calin.tcp.http.response.HttpResponse;
import ro.calin.tcp.http.response.HttpStatus;
import ro.calin.tcp.http.route.RequestHandler;

/**
 * @author cavasilcai
 */
public class WebappRequestHandler implements RequestHandler {
    @Override
    public HttpResponse handle(HttpRequest request) {
        return HttpResponse
                .status(HttpStatus.OK)
                .header("Content-Type", "text/html")
                .body(WebappRequestHandler.class.getResourceAsStream("/iframe-drag-drop.html"));
    }
}
