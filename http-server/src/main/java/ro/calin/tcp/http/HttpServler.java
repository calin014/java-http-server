package ro.calin.tcp.http;

/**
 * @author calin
 */
public interface HttpServler {
    void serve(HttpRequest request, HttpResponse response);
}
