package org.jmxline.app.http;

import java.util.Map;

import org.jmxline.app.JmxLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServerWrapper {

	private static final Logger logger = LoggerFactory.getLogger(HttpServerWrapper.class);
    private static final String DEFAULT_JMX_HOST = "localhost";
    private static final int DEFAULT_JMX_PORT = 8080;
    private static EmbeddedServer server;

    /**
     * Start a HTTP webserver.
     */
    public static void startServer() {

        System.out.println("Starting server...");

        startSimpleServer();
    }

    private static void startSimpleServer() {
        try {
            server = EmbeddedServer.createInstance(8282, new HttpRequestHandler() {
                @Override
                public HttpResponse handleRequest(Type type, String url, Map<String, String> parameters) {
                    HttpResponse response = new HttpResponse();

                    // TODO: Should these be changed and passed via HTTP?
                    String jmxHost = System.getProperty("JMX_HOST") != null ? System.getProperty("JMX_HOST")
                            : DEFAULT_JMX_HOST;
                    Integer jmxPort = System.getProperty("JMX_PORT") != null ? Integer.parseInt(System
                            .getProperty("JMX_PORT")) : DEFAULT_JMX_PORT;

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
}
