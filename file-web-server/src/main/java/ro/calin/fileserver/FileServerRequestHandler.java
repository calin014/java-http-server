package ro.calin.fileserver;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import ro.calin.tcp.http.request.HttpRequest;
import ro.calin.tcp.http.response.HttpResponse;
import ro.calin.tcp.http.response.HttpStatus;
import ro.calin.tcp.http.route.RequestHandler;

/**
 * @author cavasilcai
 */
public class FileServerRequestHandler implements RequestHandler {
    private static String LISTING_TEMPLATE =
                    "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\">\n" +
                    "<html>\n" +
                    " <head>\n" +
                    "  <title>Index of {0}</title>\n" +
                    " </head>\n" +
                    " <body>\n" +
                    "<h1>Index of {0}</h1>\n" +
                    "<ul>\n{1}</ul>\n" +
                    "</body></html>";
    private static String LINK_TEMPLATE = "<li><a href=\"{0}\"> {1}</a></li>\n";

    static private Map<String, String> types = new HashMap<String, String>();
    static {
        types.put("txt", "text/plain");
        types.put("html", "text/html");
        types.put("css", "text/css");
        types.put("jpg", "image/jpeg");
        types.put("png", "image/png");
        types.put("js", "application/javascript");
        //TODO: add more
    }

    private String root;

    public FileServerRequestHandler(String root) {
        this.root = root;
    }

    @Override
    public HttpResponse handle(HttpRequest request) {
        //TODO: do not allow access to upper: eg.: ../smfn        ???
        File file = new File(root, request.getUrl());
        if (!file.exists()) {
            return HttpResponse.status(HttpStatus.NOT_FOUND);
        } else if (file.isDirectory()) {
            return directoryListingResponse(file, request.getUrl());
        } else {
            return fileResponse(file);
        }
    }

    private HttpResponse fileResponse(File file) {
        try {
            return HttpResponse
                    .status(HttpStatus.OK)
                    .header("Content-Type", guessMimeType(file))
                    .header("Content-Length", file.length())
                    .body(new FileInputStream(file));
        } catch (IOException e) {
            if (e instanceof FileNotFoundException)
                return HttpResponse.status(HttpStatus.NOT_FOUND);
            else return HttpResponse.status(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String guessMimeType(File file) {
        final String mime = types.get(FilenameUtils.getExtension(file.getName()));
        return mime == null? "application/octet-stream" : mime;
    }

    private HttpResponse directoryListingResponse(File directory, String path) {
        try {
            String html = buildListening(directory, path);
            InputStream htmlStream = IOUtils.toInputStream(html, "UTF-8");
            return HttpResponse
                    .status(HttpStatus.OK)
                    .header("Content-Type", "text/html")
                    .body(htmlStream);
        } catch (Exception e) {
            return HttpResponse.status(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String buildListening(File directory, String path) {
        StringBuilder sb = new StringBuilder();

        if (!"/".equals(path)) {
            sb.append(LINK_TEMPLATE
                    .replaceAll("\\{0\\}", parentAbsolutePath(path) + "/")
                    .replaceAll("\\{1\\}", "Parent Directory")
            );
        }

        File[] files = directory.listFiles();
        for (File file:files) {
            String name = file.getName();
            if (file.isDirectory()) {
                name += '/';
            }
            sb.append(LINK_TEMPLATE
                    .replaceAll("\\{0\\}", name)
                    .replaceAll("\\{1\\}", name)
            );
        }

        return LISTING_TEMPLATE
                .replaceAll("\\{0\\}", path)
                .replaceAll("\\{1\\}", sb.toString());
    }

    private String parentAbsolutePath(String path) {
        if(path.endsWith("/")) path = path.substring(0, path.length() - 1);
        int idx = path.lastIndexOf('/');
        return path.substring(0, idx);
    }
}
