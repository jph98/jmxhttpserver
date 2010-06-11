package org.jmxline.app;

import java.lang.instrument.Instrumentation;

import org.jmxline.app.http.HttpServerWrapper;

public class JmxLineAgent {
    
    /**
     * Premain-Class for agent.
     */
    public static void premain(String agentArgs, Instrumentation inst) {

        System.out.println("JmxLine premain called...");
        Runnable runnableServer = new Runnable() {

            @Override
            public void run() {
                HttpServerWrapper.startServer();
            }
        };
        Thread serverThread = new Thread(runnableServer);
        serverThread.setDaemon(true);
        serverThread.start();

    }

    /**
     * Main method.
     * 
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("JmxLine main called...");
        Runnable runnableServer = new Runnable() {

            @Override
            public void run() {
                HttpServerWrapper.startServer();
            }
        };
        Thread serverThread = new Thread(runnableServer);

        if (args.length == 1 && args[0].equals("fg")) {
            // Will start the server as part of the main user thread
            serverThread.setDaemon(false);
        } else {
            // JVM will die regardless of running daemon threads
            serverThread.setDaemon(true);
        }
        serverThread.start();
    }
}
