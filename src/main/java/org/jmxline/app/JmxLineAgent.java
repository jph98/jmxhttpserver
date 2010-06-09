package org.jmxline.app;

import java.lang.instrument.Instrumentation;
import java.net.ServerSocket;

public class JmxLineAgent {

    /**
     * A reference to the {@link java.lang.instrument.Instrumentation} instance
     * passed to this agent's {@link #premain} method.  This way we can keep using
     * the Instrumentation functionality!
     **/
    static private Instrumentation instrumentation = null;
    
    /**
     * Premain-Class: javahowto.JavaAgent
     * jar cvfm ../../myagent.jar ../../mymanifest.mf javahowto/MyAgent.class
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("JmxLine premain called...");
        
        instrumentation = inst;

        startServer();
        
    }
    
    public static void main(String [] args) {
        startServer();
        
    }

    private static void startServer() {
        try {
            ServerSocket s = new ServerSocket(8181);            
            System.out.println("Started server on port: " + 8181);
            for (;;) {
                new TinyHTTP(s.accept());
            }
        } catch (Exception e) {
        }
    }
}
