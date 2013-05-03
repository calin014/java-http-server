package ro.calin.tcp.http.request;

/**
 * @author calin
 */
public enum HttpVersion {
    HTTP10("HTTP/1.0"),
    HTTP11("HTTP/1.1");

    private final String version;

    HttpVersion(String version) {
        this.version = version;
    }

    public static HttpVersion getVersion(String value) {
        if(value == null)
            throw new IllegalArgumentException();
        for(HttpVersion enumValue : values())
            if(value.equalsIgnoreCase(enumValue.version)) return enumValue;
        throw new IllegalArgumentException();
    }

    public String getVersion() {
        return version;
    }

}
