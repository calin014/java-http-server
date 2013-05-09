package ro.calin.tcp.http.request.parser;

import ro.calin.tcp.http.request.HttpMethod;
import ro.calin.tcp.http.request.HttpRequest;
import ro.calin.tcp.http.request.HttpVersion;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * @author calin
 */
public class BasicHttpRequestParser implements HttpRequestParser {
    final static Logger LOGGER = Logger.getLogger(BasicHttpRequestParser.class);
    public static final char CR = '\r';
    public static final char LF = '\n';

    @Override
    public HttpRequest parse(InputStream stream) throws BadRequestException {
        HttpRequest request = new HttpRequest();

        try {
            parseMethodAndUrl(getNextLine(stream), request);
            parseHeaders(stream, request);
            readAndParseBody(stream, request);
        } catch (IOException e) {
            throwBadRequest();
        }
        return request;
    }

    private static String getNextLine(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        int b;
//        int prev = -1;
        while((b = is.read()) != -1) {
            if(b != CR && b != LF) baos.write(b);
            if(/*prev == CR &&*/ b == LF) break;  //handle also LF alone
//            prev = b;
        }

        return baos.toString("UTF-8");
    }

    private void readAndParseBody(InputStream stream, HttpRequest request) throws
            IOException, BadRequestException {
        final String contentLength = request.headerValue("Content-Length");
        if(contentLength != null) {
            try {
                final int len = Integer.parseInt(contentLength);
                byte[] body = new byte[len];
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

    private void parseHeaders(InputStream in, HttpRequest request) throws IOException, BadRequestException {
        String line;
        String name = null;
        String value;
        while (!"".equals(line = getNextLine(in))) { //second CRLF
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

    private void parseUrl(String url, HttpRequest request) throws BadRequestException {
        String[] tokens = url.split("\\?");
        try {
            //TODO: search encoding in Content-Type header???
            request.setUrl(URLDecoder.decode(tokens[0], "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throwBadRequest();
        }

        if(tokens.length > 1) parseUrlParams(tokens[1], request);
    }

    private void parseUrlParams(String urlParams, HttpRequest request) throws BadRequestException {
        String[] tokens = urlParams.split("&");
        for (String param : tokens) {
            String[] keyval = param.split("=");
            if(keyval.length == 2) {
                try {
                    request.param(URLDecoder.decode(keyval[0], "UTF-8"), URLDecoder.decode(keyval[1], "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    throwBadRequest();
                }
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
