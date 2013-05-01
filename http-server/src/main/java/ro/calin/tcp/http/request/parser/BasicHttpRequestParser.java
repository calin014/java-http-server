package ro.calin.tcp.http.request.parser;

import ro.calin.tcp.http.request.HttpMethod;
import ro.calin.tcp.http.request.HttpRequest;
import ro.calin.tcp.http.response.HttpResponse;

import java.io.*;

/**
 * @author calin
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
                parseHeader(line, request);
            }

            //TODO: if it makes sense? eg.: not GET???
            request.setBody(stream);

            in.close();
        } catch (IOException e) {
            throwBadRequest();
        }
        return request;
    }

    private void parseHeader(String line, HttpRequest request) throws BadRequestException {
        int i = line.indexOf(':');
        if(i == -1) throwBadRequest();

        request.header(line.substring(0, i), line.substring(i + 1));
    }

    private void parseMethodAndUrl(String line, HttpRequest request) throws BadRequestException {
        String[] split = line.split(" ");

        if(split.length != 3) throwBadRequest();
        if (!acceptedProtocolSpec(split[2])) throwBadRequest();

        parseMethod(split[0], request);
        parseUrl(split[1], request);
    }

    private void parseUrl(String url, HttpRequest request) {
        String[] split = url.split("\\?");
        request.setUrl(split[0]);

        if(split.length > 1) parseUrlParams(split[1], request);
    }

    private void parseUrlParams(String urlParams, HttpRequest request) {
        String[] split = urlParams.split("&");
        for (String param : split) {
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

    private boolean acceptedProtocolSpec(String protSpec) {
        return protSpec.equals("HTTP/1.0") || protSpec.equals("HTTP/1.1");
    }

    private void throwBadRequest() throws BadRequestException {
        throw new BadRequestException(new HttpResponse());
    }
}
