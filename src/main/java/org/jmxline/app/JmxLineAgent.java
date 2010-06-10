package org.jmxline.app;

import java.lang.instrument.Instrumentation;

import org.jmxline.app.http.GrizzlyHttp;

import com.sun.grizzly.http.SelectorThread;

public class JmxLineAgent {

    /**
     * A reference to the {@link java.lang.instrument.Instrumentation} instance
     * passed to this agent's {@link #premain} method.  This way we can keep using
     * the Instrumentation functionality!
     **/
    static private Instrumentation instrumentation = null;
    
    /**
     * Premain-Class: javahowto.JavaAgent     
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        
        System.out.println("JmxLine premain called...");
        Runnable runnableServer = new Runnable() {
            
            @Override
            public void run() {
                startServer();                
            }
        };
        Thread serverThread = new Thread(runnableServer);
        serverThread.setDaemon(true);
        serverThread.start();
        
    }
    
    /**
     * Main method.
     * @param args
     */
    public static void main(String [] args) {
        System.out.println("JmxLine main called...");
        Runnable runnableServer = new Runnable() {
            
            @Override
            public void run() {
                startServer();                
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

    /**
     * Start a Grizzly server.
     */
    public static void startServer() {
        
        System.out.println("Starting server...");
        SelectorThread st = new SelectorThread();
        int port = 8282;
        st.setPort(port);        
        
        GrizzlyHttp httpServer = new GrizzlyHttp();        
        st.setAdapter(httpServer);
        try {            
            System.out.println("Init endpoint");
            st.initEndpoint();
            System.out.println("Start endpoint");
            st.startEndpoint();
            System.out.println("Started server on " + st.getAddress() + ":" + st.getPort());
        } catch (Exception e) {
            System.out.println("Exception in SelectorThread: " + e);
        } finally {
            if (st.isRunning()) {
                st.stopEndpoint();
                System.out.println("Stopped server");
            }
        }        
    }
}
