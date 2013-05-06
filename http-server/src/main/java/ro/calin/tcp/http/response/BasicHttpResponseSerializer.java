package ro.calin.tcp.http.response;

import org.apache.commons.io.IOUtils;
import ro.calin.tcp.http.request.HttpVersion;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author calin
 */
public class BasicHttpResponseSerializer implements HttpResponseSerializer {
    private static final String CRLF = "\r\n";

    @Override
    public void serialize(HttpResponse response, OutputStream stream) {
        PrintWriter pw = new PrintWriter(stream);

        writeVersionAndStatus(pw, response.getStatus());
        writeHeaders(pw, response.getHeaders());
        writeDateHeaderIfNecessary(pw, response.getHeaders());
        writeBody(stream, pw, response);
    }

    private void writeVersionAndStatus(PrintWriter pw, HttpStatus status) {
        pw.print(HttpVersion.HTTP11.getVersion() + " " + status.getStatus() + CRLF);
    }

    private void writeHeaders(PrintWriter pw, Map<String, List<String>> headers) {
        for (Map.Entry<String, List<String>> header : headers.entrySet()) {
            for (String value : header.getValue()) {
                writeHeader(pw, header.getKey(), value);
            }
        }
    }

    private void writeHeader(PrintWriter pw, String name, String value) {
        pw.print(name.trim() + ": " + value.trim() + CRLF);
    }

    private void writeDateHeaderIfNecessary(PrintWriter pw, Map<String, List<String>> headers) {
        //copied from https://github.com/NanoHttpd/nanohttpd/blob/master/core/src/main/java/fi/iki/elonen/NanoHTTPD.java
        if (headers == null || headers.get("Date") == null) {
            SimpleDateFormat gmtFrmt = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
            gmtFrmt.setTimeZone(TimeZone.getTimeZone("GMT"));
            writeHeader(pw, "Date", gmtFrmt.format(new Date()));
        }
    }

    private void writeBody(OutputStream stream, PrintWriter pw, HttpResponse response) {
        //write content length and type if need
        InputStream body = response.getBody();
        byte[] loaded = null;
        long len = 0;
        if(body != null) {
            try {
                len = Long.parseLong(response.headerValue("Content-Length"));
            } catch (Exception e) {
                try {
                    loaded = IOUtils.toByteArray(body);
                    len = loaded.length;
                    writeHeader(pw, "Content-Length", String.valueOf(len));
                } catch (IOException e1) {
                    //500 would make sense but cannot change status now
                    //TODO log
                }
            }

            if(!response.hasHeader("Content-Type")) {
                writeHeader(pw, "Content-Type", "application/octet-stream");
            }
        }

        pw.print(CRLF);
        pw.flush();

        if(body != null) {
            try {
                if(loaded != null) {
                    stream.write(loaded);
                } else {
                    IOUtils.copyLarge(body, stream, 0, len);
                }
            } catch (IOException e) {
                //500 would make sense but cannot change status now
                //TODO log
            } finally {
                IOUtils.closeQuietly(body);
            }
        }
    }
}
