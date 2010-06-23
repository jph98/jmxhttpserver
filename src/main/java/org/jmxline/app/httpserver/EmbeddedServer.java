/**
 * Copyright 2006-2007, subject to LGPL version 3
 * User: garethc
 * Date: Mar 13, 2007
 * Time: 2:11:17 PM
 */
package org.jmxline.app.httpserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ultra lightweight web server for embedding in applications.
 * Originally adapted from VQEmbWeb (http://www.croninsolutions.com/vqembweb/) 
 */
public class EmbeddedServer {

	private final static Logger logger = LoggerFactory.getLogger(EmbeddedServer.class);

    /**
     * Port to serve clients on
     */
    private int serverPort;

    /**
     * Server lives as long as this is true
     */
    private volatile boolean alive;

    /**
     * Handler
     */
    private HttpRequestHandler clientHandler;

    /**
     * Create a new instance of an embedded server on a specific port with a set of handlers.
     * 
     * @param serverPort
     * @throws Exception
     */
    public static EmbeddedServer createInstance(int serverPort, HttpRequestHandler handler) throws Exception {
        final EmbeddedServer server = new EmbeddedServer(serverPort, handler);
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    server.start();
                } catch (IOException e) {
                    System.err.println("Failed to start server" + e);
                }
            }
        }, "server thread");
        thread.start();        
        return server;
    }

    /**
     * New instance
     * 
     * @param serverPort
     * @param handler
     * @throws Exception
     */
    private EmbeddedServer(int serverPort, HttpRequestHandler handler) throws Exception {
        this.serverPort = serverPort;
        this.clientHandler = handler;
    }

    /**
     * Start the server
     * 
     * @throws java.io.IOException
     */
    public void start() throws IOException {
        this.alive = true;
        ServerSocket serverSocket;
        
        // Create a secure socket if we can, otherwise standard socket on default server port
        if (System.getProperty("javax.net.ssl.keyStore") != null) {
            ServerSocketFactory ssocketFactory = SSLServerSocketFactory.getDefault();
            serverSocket = ssocketFactory.createServerSocket(this.serverPort);
        } else {
            serverSocket = new ServerSocket(this.serverPort);
        }
        
        logger.info("Server up on " + InetAddress.getLocalHost().getHostName() + ":" + this.serverPort);
        
        // 
        while (alive) {
            // Wrap request handler and start a thread for each one
            // TODO: Improve this to pool threads using executor service.
            Socket clientRequestSocket = serverSocket.accept();
            
            String clientHost = clientRequestSocket.getInetAddress().getCanonicalHostName();
                        
            Thread thread = new Thread(new RequestHandler(clientRequestSocket), "ClientRequest-" + clientHost);
            thread.start();
        }
        
        logger.debug("exiing accept thread");
        
    }

    /**
     * Request handler
     */
    private class RequestHandler implements Runnable {

        /**
         * Socket to handle the request on
         */
        private Socket clientRequestSocket;

        /**
         * New handler
         * 
         * @param clientRequestSocket
         *            Socket to handle the request on
         */
        public RequestHandler(Socket clientRequestSocket) {
            this.clientRequestSocket = clientRequestSocket;
        }

        /**
         * Handle the request
         * 
         * @see Thread#run()
         */
        public void run() {
            try {
                InputStream requestInputStream = clientRequestSocket.getInputStream();
                
                // Create default reader with buffer of 8192 bytes.
                BufferedReader reader = new BufferedReader(new InputStreamReader(requestInputStream));
                String line = null;
                while ((line = reader.readLine()) != null) {

                    if (line.startsWith("GET") || line.startsWith("POST")) {

                        // TODO: Change this to a blocked path list
                        if (line.contains("favicon.ico")) {
                            return;
                        }
                            
                        StringTokenizer tokenizer = new StringTokenizer(line, " ");
                        String requestType = tokenizer.nextToken();
                        String url = tokenizer.nextToken();

                        Map<String, String> parameters = new HashMap<String, String>();

                        int indexOfQuestionMark = url.indexOf("?");
                        if (indexOfQuestionMark >= 0) {
                            // there are URL parameters
                            String parametersToParse = url.substring(indexOfQuestionMark + 1);
                            url = url.substring(0, indexOfQuestionMark);
                            StringTokenizer parameterTokenizer = new StringTokenizer(parametersToParse, "&");
                            while (parameterTokenizer.hasMoreTokens()) {
                                String[] keyAndValue = parameterTokenizer.nextToken().split("=");
                                String key = URLDecoder.decode(keyAndValue[0], "utf-8");
                                String value = URLDecoder.decode(keyAndValue[1], "utf-8");
                                parameters.put(key, value);
                            }
                        }

                        // Default
                        HttpRequestHandler.Type type = HttpRequestHandler.GET;

                        if ("GET".equals(requestType)) {
                            type = HttpRequestHandler.GET;
                        } else if ("POST".equals(requestType)) {
                            type = HttpRequestHandler.POST;
                        }

                        HttpResponse response = EmbeddedServer.this.clientHandler.handleRequest(type, url, parameters);

                        OutputStream outputStream = clientRequestSocket.getOutputStream();
                        response.writeToStream(outputStream);
                        outputStream.close();
                    }
                }
            } catch (SocketException e) {
                // System.err.println("Socket error" + e);
            } catch (IOException e) {
                // System.err.println("I/O Error" + e);
            }
        }

    }

}
