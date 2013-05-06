package ro.calin.tcp.http.response;

/**
 * @author calin
 */
public enum HttpStatus {
    OK(200, "OK"),
    CREATED(201, "Created"),
    NO_CONTENT(204, "No Content"),
    PARTIAL_CONTENT(206, "Partial Content"),
    MOVED_PERMANENTLY(301, "Moved Permanently"),
    NOT_MODIFIED(304, "Not Modified"),
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    RANGE_NOT_SATISFIABLE(416, "Requested Range Not Satisfiable"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error");

    private int status;
    private String desc;

    HttpStatus(int status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public String getStatus() {
        return "" + status + " " + desc;
    }
}
