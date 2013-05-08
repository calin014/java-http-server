package ro.calin.tcp.http.request.parser;

import ro.calin.tcp.http.request.HttpMethod;
import ro.calin.tcp.http.request.HttpRequest;
import ro.calin.tcp.http.request.HttpVersion;

import java.io.*;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * @author calin
 */
public class BasicHttpRequestParser implements HttpRequestParser {
    final static Logger LOGGER = Logger.getLogger(BasicHttpRequestParser.class);

    @Override
    public HttpRequest parse(InputStream stream) throws BadRequestException {
        HttpRequest request = new HttpRequest();

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(stream));
            parseMethodAndUrl(in.readLine(), request);
            parseHeaders(in, request);

            readAndParseBody(stream, request);
        } catch (IOException e) {
            throwBadRequest();
        }
        return request;
    }

    private void readAndParseBody(InputStream stream, HttpRequest request) throws
            IOException {
        final String contentLength = request.headerValue("Content-Length");
        if(contentLength != null) {
            try {
                final int len = Integer.parseInt(contentLength);
                byte[] body = new byte[len];
                //TODO: the stream has been read into to buffer readers buffer!!!!
                IOUtils.read(stream, body);
                request.setBody(body);

                if (request.headerValueContains("Content-Type", "application/x-www-form-urlencoded")) {
                    LOGGER.info("Content-Type is application/x-www-form-urlencoded, parsing body...");
                    parseUrlParams(new String(body), request);
                }
            } catch (NumberFormatException e) {
                LOGGER.info("Could not parse Content-Length header for request. Assuming no body.");
            }
        } else {
            LOGGER.info("No Content-Length header. Assuming no body.");
        }
    }

    private void parseHeaders(BufferedReader in, HttpRequest request) throws IOException, BadRequestException {
        String line;
        String name = null;
        String value;
        while ((line = in.readLine()) != null) {
            if ("".equals(line)) break; //CRLF line

            if(line.startsWith(" ") || line.startsWith("\t")) {
                if(name == null) throwBadRequest();
                value = line.trim();
            } else {
                int i = line.indexOf(':');
                if(i == -1) throwBadRequest();
                name = line.substring(0, i).trim();
                value = line.substring(i + 1).trim();
            }

            request.header(name, value);
        }
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
