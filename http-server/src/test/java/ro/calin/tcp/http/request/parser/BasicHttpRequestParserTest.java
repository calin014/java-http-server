package ro.calin.tcp.http.request.parser;

import org.junit.Assert;
import org.junit.Test;
import ro.calin.tcp.http.request.HttpRequest;

import java.io.ByteArrayInputStream;

import static ro.calin.tcp.http.request.HttpMethod.GET;

/**
 * @author calin
 */
public class BasicHttpRequestParserTest {
    private BasicHttpRequestParser parser = new BasicHttpRequestParser();

    @Test
    public void simpleTest() throws BadRequestException {
        String request =
                "GET /easy/http/?test=a&test=b&othertest=c HTTP/1.1\n" +
                "Host: www.jmarshall.com\n" +
                "Connection: keep-alive\n" +
                "Cache-Control: max-age=0\n" +
                "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\n" +
                "User-Agent: Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.64 Safari/537.31\n" +
                "Referer: https://www.google.ro/\n" +
                "Accept-Encoding: gzip,deflate,sdch\n" +
                "Accept-Language: en-US,en;q=0.8,ro;q=0.6\n" +
                "Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.3\n" +
                "Cookie: _tb_pingSent=1\n" +
                "If-None-Match: \"1d69940-b070-50c690d7\"\n" +
                "If-Modified-Since: Tue, 11 Dec 2012 01:48:07 GMT\n" +
                "\n";
        ByteArrayInputStream requestStream = new ByteArrayInputStream(request.getBytes());

        HttpRequest actual = parser.parse(requestStream);
        HttpRequest expected = new HttpRequest()
                .method(GET)
                .url("/easy/http/")
                .param("test", "a")
                .param("test", "b")
                .param("othertest", "c")
                .header("Host", "www.jmarshall.com")
                .header("Connection", "keep-alive")
                .header("Cache-Control", "max-age=0")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.64 Safari/537.31")
                .header("Referer", "https://www.google.ro/")
                .header("Accept-Encoding", "gzip,deflate,sdch")
                .header("Accept-Language", "en-US,en;q=0.8,ro;q=0.6")
                .header("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3")
                .header("Cookie", "_tb_pingSent=1")
                .header("If-None-Match", "\"1d69940-b070-50c690d7\"")
                .header("If-Modified-Since", "Tue, 11 Dec 2012 01:48:07 GMT");

        Assert.assertEquals(expected, actual);
    }
}
