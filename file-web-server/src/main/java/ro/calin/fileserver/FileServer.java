package ro.calin.fileserver;

import java.io.*;

import org.apache.log4j.BasicConfigurator;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import ro.calin.tcp.http.Server;

import static ro.calin.tcp.http.request.HttpMethod.GET;

/**
 * @author cavasilcai
 */
public class FileServer {

    public static void main(String[] args) throws IOException {
        ServerOptions o = new ServerOptions();
        CmdLineParser parser = new CmdLineParser(o);

        try {
            parser.parseArgument(args);
        } catch(CmdLineException e ) {
            System.err.println(e.getMessage());
            System.err.println("java -jar file-web-server.jar options\nOptions:");
            parser.printUsage(System.err);
            return;
        }

        BasicConfigurator.configure();

        final Server server = Server.create()
                .workers(o.workers)
                .port(o.port)
                .keepAlive(o.keepAlive)
                .route(GET, ".*", new FileServerRequestHandler(o.root))
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

    private static class ServerOptions {
        @Option(name="-r", required = true, usage="folder from where files will be served, mandatory")
        private String root;
        @Option(name="-p", usage="the port on which the server will listen, default 80")
        private int port = 80;
        @Option(name="-w", usage="number of worker threads, default 10")
        private int workers = 15;
        @Option(name="-ka", usage="reuse connection for subsequent requests")
        private boolean keepAlive;
    }
}
