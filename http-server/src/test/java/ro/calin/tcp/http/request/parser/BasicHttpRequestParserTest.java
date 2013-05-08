package ro.calin.tcp.http.request.parser;

import org.junit.Assert;
import org.junit.Test;
import ro.calin.tcp.http.request.HttpRequest;
import ro.calin.tcp.http.request.HttpVersion;

import java.io.ByteArrayInputStream;

import static ro.calin.tcp.http.request.HttpMethod.GET;
import static ro.calin.tcp.http.request.HttpMethod.POST;

/**
 * @author calin
 */
public class BasicHttpRequestParserTest {
    private BasicHttpRequestParser parser = new BasicHttpRequestParser();

    @Test
    public void testParsingGET() throws BadRequestException {
        String request =
                "GET /easy/http/?test=a&test=b&othertest=c%26d%26e \tHTTP/1.1\r\n" +
                "Host: www.jmarshall.com\r\n" +
                "Connection: keep-alive\n" +
                "Cache-Control: max-age=0\r\n" +
                "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\n" +
                "User-Agent: Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.64 Safari/537.31\n" +
                "Referer: https://www.google.ro/\n" +
                "Accept-Encoding: gzip,deflate,sdch\n" +
                "Accept-Language: en-US,en;q=0.8,ro;q=0.6\n" +
                "Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.3\n" +
                "Cookie: _tb_pingSent=1\r\n" +
                "If-None-Match: \"1d69940-b070-50c690d7\"\n" +
                "If-Modified-Since: Tue, 11 Dec 2012 01:48:07 GMT\n" +
                "\n";
        ByteArrayInputStream requestStream = new ByteArrayInputStream(request.getBytes());

        HttpRequest actual = parser.parse(requestStream);
        HttpRequest expected = new HttpRequest()
                .method(GET)
                .url("/easy/http/")
                .version(HttpVersion.HTTP11)
                .param("test", "a")
                .param("test", "b")
                .param("othertest", "c&d&e")
                .header("host", "www.jmarshall.com")
                .header("connection", "keep-alive")
                .header("cache-control", "max-age=0")
                .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("user-agent", "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.64 Safari/537.31")
                .header("referer", "https://www.google.ro/")
                .header("accept-encoding", "gzip,deflate,sdch")
                .header("accept-language", "en-US,en;q=0.8,ro;q=0.6")
                .header("accept-charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3")
                .header("cookie", "_tb_pingSent=1")
                .header("if-none-match", "\"1d69940-b070-50c690d7\"")
                .header("if-modified-since", "Tue, 11 Dec 2012 01:48:07 GMT");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testParsingPOST() throws BadRequestException {
        String request =
                        "POST /path/script.cgi HTTP/1.0\r\n" +
                        "From: frog@jmarshall.com\r\n" +
                        "User-Agent: HTTPTool/1.0\r\n" +
                        "Content-Type: application/x-www-form-urlencoded\r\n" +
                        "Content-Length: 32\r\n" +
                        "\r\n" +
                        "home=Cosby&favorite+flavor=flies";

        ByteArrayInputStream requestStream = new ByteArrayInputStream(request.getBytes());
        HttpRequest actual = parser.parse(requestStream);

        HttpRequest expected = new HttpRequest()
                .method(POST)
                .url("/path/script.cgi")
                .version(HttpVersion.HTTP10)
                .header("From", "frog@jmarshall.com")
                .header("User-Agent", "HTTPTool/1.0")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Content-Length", "32")
                .param("home", "Cosby")
                .param("favorite flavor", "flies")
                .body("home=Cosby&favorite+flavor=flies".getBytes());

        Assert.assertEquals(expected, actual);
    }
}
