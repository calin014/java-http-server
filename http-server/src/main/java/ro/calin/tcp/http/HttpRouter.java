package ro.calin.tcp.http;

/**
 * @author calin
 */
public interface HttpRouter {
    void addRoute(HttpMethod method, String urlPattern, HttpServler servler);
    HttpServler findRoute(HttpMethod method, String urlPattern);
}
