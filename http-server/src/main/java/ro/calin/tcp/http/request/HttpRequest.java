package ro.calin.tcp.http.request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author calin
 */
public class HttpRequest {
    private HttpMethod method;
    private String url;
    private HttpVersion version;

    private Map<String, List<String>> parameters;

    private Map<String, String> headers;

    private byte[] body;

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public HttpVersion getVersion() {
        return version;
    }

    public void setVersion(HttpVersion version) {
        this.version = version;
    }

    public Map<String, List<String>> getParameters() {
        return parameters;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public HttpRequest method(HttpMethod method) {
        setMethod(method);
        return this;
    }

    public HttpRequest url(String url) {
        setUrl(url);
        return this;
    }

    public HttpRequest version(HttpVersion version) {
        setVersion(version);
        return this;
    }

    public HttpRequest header(String name, String value) {
        if (headers == null) headers = new HashMap<String, String>();
        //headers are case insensitive
        name = name.toLowerCase();

        //multiple values are comma separated
        String current = headers.get(name);
        current = current == null? value: current + ", " + value;
        headers.put(name, current);

        return this;
    }

    public HttpRequest param(String name, String value) {
        if (parameters == null) parameters = new HashMap<String, List<String>>();
        add(parameters, name, value);
        return this;
    }

    public HttpRequest body(byte[] body) {
        setBody(body);
        return this;
    }

    private void add(Map<String, List<String>> dest, String name, String value) {
        List<String> list = dest.get(name);
        if (list == null) {
            list = new ArrayList<String>();
            dest.put(name, list);
        }
        list.add(value);
    }

    public boolean headerValueContains(String key, String value) {
        key = key.toLowerCase();
        return headers != null && headers.get(key) != null && headers.get(key).contains(value);
    }

    public String headerValue(String name) {
        return headers.get(name.toLowerCase());
    }


    public String paramValue(String name) {
        final List<String> params = parameters.get(name.toLowerCase());
        return params == null || params.size() == 0? null : params.get(0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HttpRequest that = (HttpRequest) o;

        if (!Arrays.equals(body, that.body)) return false;
        if (headers != null ? !headers.equals(that.headers) : that.headers != null) return false;
        if (method != that.method) return false;
        if (parameters != null ? !parameters.equals(that.parameters) : that.parameters != null) return false;
        if (!url.equals(that.url)) return false;
        if (version != that.version) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = method.hashCode();
        result = 31 * result + url.hashCode();
        result = 31 * result + version.hashCode();
        result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
        result = 31 * result + (headers != null ? headers.hashCode() : 0);
        result = 31 * result + (body != null ? Arrays.hashCode(body) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "\n\tHttpRequest{" +
                "\n\tmethod=" + method +
                ",\n\turl='" + url + '\'' +
                ",\n\tversion=" + version +
                ",\n\tparameters=" + parameters +
                ",\n\theaders=" + headers +
                ",\n\tbody=" + (body == null? null : "[...]") +
                '}';
    }
}
