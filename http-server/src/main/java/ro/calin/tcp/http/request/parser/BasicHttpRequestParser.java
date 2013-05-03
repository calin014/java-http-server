package ro.calin.tcp.http.request.parser;

import ro.calin.tcp.http.request.HttpMethod;
import ro.calin.tcp.http.request.HttpRequest;
import ro.calin.tcp.http.request.HttpVersion;
import ro.calin.tcp.http.response.HttpResponse;

import java.io.*;

/**
 * @author calin
 *
 * @see https://github.com/NanoHttpd/nanohttpd/blob/master/core/src/main/java/fi/iki/elonen/NanoHTTPD.java
 */
public class BasicHttpRequestParser implements HttpRequestParser {
    @Override
    public HttpRequest parse(InputStream stream) throws BadRequestException {
        HttpRequest request = new HttpRequest();

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(stream));

            parseMethodAndUrl(in.readLine(), request);

            String line;
            while ((line = in.readLine()) != null) {
                if ("".equals(line)) break;
                //TODO: headers can be on multiple rows(subsequent rows start with space or tab)
                parseHeader(line, request);
            }

            //TODO: if it makes sense? eg.: not GET???
            request.setBody(stream);
        } catch (IOException e) {
            throwBadRequest();
        }
        return request;
    }

    private void parseHeader(String line, HttpRequest request) throws BadRequestException {
        int i = line.indexOf(':');
        if(i == -1) throwBadRequest();

        request.header(line.substring(0, i).trim(), line.substring(i + 1).trim());
    }

    private void parseMethodAndUrl(String line, HttpRequest request) throws BadRequestException {
        String[] tokens = line.split("\\s+");

        if(tokens.length != 3) throwBadRequest();

        parseVersion(tokens[2], request);
        parseMethod(tokens[0], request);
        parseUrl(tokens[1], request);
    }

    private void parseVersion(String version, HttpRequest request) throws BadRequestException {
        try {
            request.version(HttpVersion.getVersion(version));
        } catch (IllegalArgumentException e) {
            throwBadRequest();
        }
    }

    private void parseUrl(String url, HttpRequest request) {
        String[] tokens = url.split("\\?");
        request.setUrl(tokens[0]);

        if(tokens.length > 1) parseUrlParams(tokens[1], request);
    }

    private void parseUrlParams(String urlParams, HttpRequest request) {
        String[] tokens = urlParams.split("&");
        for (String param : tokens) {
            String[] keyval = param.split("=");
            if(keyval.length == 2) {
                request.param(keyval[0], keyval[1]);
            }
        }
    }

    private void parseMethod(String method, HttpRequest request) throws BadRequestException {
        try {
            request.setMethod(HttpMethod.valueOf(method));
        } catch (IllegalArgumentException e) {
            throwBadRequest();
        }
    }

    private void throwBadRequest() throws BadRequestException {
        throw new BadRequestException();
    }
}
