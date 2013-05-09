package ro.calin.tcp.http.response;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author cavasilcai
 */
public class BasicHttpResponseSerializerTest {
    private BasicHttpResponseSerializer responseSerializer = new BasicHttpResponseSerializer();

    @Test
    public void testSerialize() throws Exception {
        byte[] bodyBytes = "{'test': 'this is a value'}".getBytes();
        InputStream body = new ByteArrayInputStream(bodyBytes);
        HttpResponse response = HttpResponse
                .status(HttpStatus.OK)
                .header("Some-Special-Header", "test")
                .header("Content-Type", "application/json")
                .body(body);

        OutputStream os = new ByteArrayOutputStream();
        responseSerializer.serialize(response, os);

        String expected = "HTTP/1\\.1 200 OK\\r\\n" +
                "content-type: application/json\\r\\n" +
                "some-special-header: test\\r\\n" +
                "Date: .*\\r\\n" +
                "Content-Length: " + bodyBytes.length + "\\r\\n" +
                "\\r\\n" +
                "\\{'test': 'this is a value'\\}";

        Assert.assertTrue(os.toString().matches(expected));
    }
}
