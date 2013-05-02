package ro.calin.tcp.http.response;

/**
 * @author calin
 */
public enum HttpStatus {
    OK(200, "OK"), NOT_FOUND(404, "Not Found");

    HttpStatus(int status, String desc) {

    }
}
