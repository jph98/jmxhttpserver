package org.jmxline.app.httpserver;

import java.util.Map;

import org.jmxline.app.cli.JmxLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServerWrapper {

	private static final Logger logger = LoggerFactory.getLogger(HttpServerWrapper.class);
	
	public static final String DEFAULT_HTTP_HOST = "localhost";
	public static final int DEFAULT_HTTP_PORT = 8282;
	
	private static EmbeddedServer server;
	
    public static final String DEFAULT_JMX_HOST = "localhost";
    public static final int DEFAULT_JMX_PORT = 8080;
       
    
    public static void startServer() {
        startServer(DEFAULT_HTTP_PORT, DEFAULT_JMX_HOST, DEFAULT_JMX_PORT);
    }
    
    /**
     * Start a HTTP webserver.
     */
    public static void startServer(final int httpPort, final String jmxHost, final int jmxPort) {

        System.out.println("Starting server...");

        try {
            server = EmbeddedServer.createInstance(httpPort, new HttpRequestHandler() {
                @Override
                public HttpResponse handleRequest(Type type, String url, Map<String, String> parameters) {
                    HttpResponse response = new HttpResponse();

                    JmxLine line = new JmxLine(jmxHost, jmxPort);

                    url = url.replaceFirst("/", "");
                    String[] parts = url.split("/");

                    String responseText = null;

                    if (parts.length < 2) {
                        responseText = "JMXLine: No JMX bean or attribute name specified, e.g http://localhost:8080/java.lang:type=Threading/PeakThreadCount";
                    }
                    
                    if (parts.length == 2) {
                        System.out.println("JMX Request for " + parts[0] + " attr " + parts[1]);
                        responseText = line.getAttribute(parts[0], parts[1]);
                    }

                    response.addContent(responseText);
                    response.setOk();
                    return response;
                }
            });

        } catch (Exception e) {
            logger.error("Exception " + e);
        }
    }

    public static EmbeddedServer getServer() {
        return server;
    }
    
    public static void main(String[] args) {
        new HttpServerWrapper().startServer();
    }
}
