package ro.calin.tcp.http.route;

import ro.calin.tcp.http.request.HttpMethod;

/**
 * @author calin
 */
public interface HttpRouter {
    void addRoute(HttpMethod method, String urlPattern, RequestHandler servler);
    RequestHandler findRoute(HttpMethod method, String url);
}
