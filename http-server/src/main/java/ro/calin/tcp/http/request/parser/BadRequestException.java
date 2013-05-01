package ro.calin.tcp.http.request.parser;

import ro.calin.tcp.http.response.HttpResponse;

/**
 * @author calin
 */
public class BadRequestException extends Exception {
    public HttpResponse getResponse() {
        return response;
    }

    private HttpResponse response;

    public BadRequestException(HttpResponse response) {
        this.response = response;
    }
}
