package ro.calin.tcp.http.route;

import ro.calin.tcp.http.request.HttpMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * @author calin
 */
public class BasicHttpRouter implements HttpRouter {
    private List<Route> routes;

    public BasicHttpRouter() {
        routes = new ArrayList<Route>();
    }

    @Override
    public void addRoute(HttpMethod method, String urlPattern, RequestHandler servler) {
        synchronized (routes) {
            routes.add(new Route(method, urlPattern, servler));
        }
    }

    @Override
    public RequestHandler findRoute(HttpMethod method, String url) {
        synchronized (routes) {
            for (Route route : routes) {
                if (method == route.method && url.matches(route.urlPattern)) {
                    return route.servler;
                }
            }
        }

        return null;
    }

    private static class Route {
        HttpMethod method;
        String urlPattern;
        RequestHandler servler;

        private Route(HttpMethod method, String urlPattern, RequestHandler servler) {
            this.method = method;
            this.urlPattern = urlPattern;
            this.servler = servler;
        }
    }
}
