package ro.calin.tcp.http;

import java.util.List;
import java.util.Map;

/**
 * @author calin
 */
public class HttpRequest {
    private HttpMethod method;
    private String url;
    private Map<String, List<String>> queryParameters;
    private Map<String, List<String>> headerParameters;

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

    public Map<String, List<String>> getQueryParameters() {
        return queryParameters;
    }

    public void setQueryParameters(Map<String, List<String>> queryParameters) {
        this.queryParameters = queryParameters;
    }

    public Map<String, List<String>> getHeaderParameters() {
        return headerParameters;
    }

    public void setHeaderParameters(Map<String, List<String>> headerParameters) {
        this.headerParameters = headerParameters;
    }
}
