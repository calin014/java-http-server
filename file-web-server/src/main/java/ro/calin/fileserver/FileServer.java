package ro.calin.fileserver;

import java.io.*;

import ro.calin.tcp.http.Server;
import ro.calin.tcp.http.request.HttpRequest;
import ro.calin.tcp.http.response.HttpResponse;
import ro.calin.tcp.http.response.HttpStatus;
import ro.calin.tcp.http.route.HttpServler;
import static ro.calin.tcp.http.request.HttpMethod.GET;

/**
 * @author cavasilcai
 */
public class FileServer {

    public static void main(String[] args) throws IOException {
        //TODO: get from cmd line
        String root = "E:\\www";
        final Server server = Server
                .create()
                .port(3000)
                .route(GET, ".*", new FileServler(root))
                .keepAlive(true)
                .start();

        waitForStop();
        server.stop();
    }

    private static void waitForStop() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String line;

        while ((line = in.readLine()) != null) {
            if ("stop".equals(line))
                break;
        }
    }

    private static class FileServler implements HttpServler {
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

        private String root;

        private FileServler(String root) {
            this.root = root;
        }

        @Override
        public void serve(HttpRequest request, HttpResponse response) {
            //TODO: do not allow access to upper: eg.: ../smfn
            File file = new File(root, request.getUrl());
            if (!file.exists()) {
                response.status(HttpStatus.NOT_FOUND);
            } else if (file.isDirectory()) {
                doDirectoryListing(file, request.getUrl(), response);
            } else {
                sendFile(file, response);
            }
        }

        private void sendFile(File file, HttpResponse response) {
            try {
                FileInputStream fis = new FileInputStream(file);
                byte[] data = new byte[1024];
                int len;

                response
                    .status(HttpStatus.OK)
                    .header("Content-Type", guessMimeType(file))
                    .header("Transfer-Encoding", "chunked");

                while((len = fis.read(data)) != -1) {
                    response.chunck(len, data);
                }
                fis.close();
            } catch (FileNotFoundException e) {
                response.status(HttpStatus.NOT_FOUND);
            } catch (IOException e) {
                //TODO: log
            }
        }

        private String guessMimeType(File file) {
            return "text/html";
        }

        private void doDirectoryListing(File directory, String path, HttpResponse response) {
            byte[] html = buildListening(directory, path).getBytes();
            response
                .status(HttpStatus.OK)
                .header("Content-Type", "text/html")
                .header("Content-Length", html.length)
                .body(html);
        }

        private String buildListening(File directory, String path) {
            StringBuilder sb = new StringBuilder();

            sb.append(LINK_TEMPLATE
                    .replaceAll("\\{0\\}", parentAbsolutePath(path))
                    .replaceAll("\\{1\\}", "Parent Directory")
            );

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
            if(path.endsWith("/")) path = path.substring(path.length() - 1);
            int idx = path.lastIndexOf('/');
            return path.substring(0, idx);
        }
    }
}
