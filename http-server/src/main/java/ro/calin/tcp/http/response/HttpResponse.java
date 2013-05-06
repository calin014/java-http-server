package ro.calin.tcp.http.response;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author calin
 */
public class HttpResponse {
    private HttpStatus status;
    private Map<String, List<String>> headers;
    private InputStream body;

    public static HttpResponse status(HttpStatus status) {
        return new HttpResponse(status);
    }

    private HttpResponse(HttpStatus status) {
        this.status = status;
        headers = new HashMap<String, List<String>>();
    }

    public HttpResponse header(String name, Object value) {
        name = name.toLowerCase();
        List<String> current = headers.get(name);
        if (current == null) {
            current = new ArrayList<String>();
            headers.put(name, current);
        }
        current.add(value.toString());
        return this;
    }

    public HttpResponse body(InputStream body) {
        this.body = body;
        return this;
    }

    public String headerValue(String name) {
        name = name.toLowerCase();
        List<String> current = headers.get(name);
        if (current == null) return null;
        return current.get(0);
    }

    public boolean hasHeader(String key, String value) {
        key = key.toLowerCase();
        return headers != null && headers.get(key) != null && headers.get(key).contains(value);
    }

    public boolean hasHeader(String key) {
        return headers != null && headers.get(key.toLowerCase()) != null;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public InputStream getBody() {
        return body;
    }
}
