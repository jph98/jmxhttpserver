package org.jmxline.app;

import java.lang.instrument.Instrumentation;
import java.net.ServerSocket;

import org.jmxline.app.http.GrizzlyHttp;
import org.jmxline.app.http.TinyHTTP;

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
     * jar cvfm ../../myagent.jar ../../mymanifest.mf javahowto/MyAgent.class
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        
        System.out.println("JmxLine premain called...");
        
//        instrumentation = inst;
//
//        startServer();
        
    }
    
    public static void main(String [] args) {
        startServer();
        
    }

    private static void startServer() {
        
        SelectorThread st = new SelectorThread();
        int port = 8282;
        st.setPort(port);
        st.setAdapter(new GrizzlyHttp());
        try {
            st.initEndpoint();
            st.startEndpoint();
        } catch (Exception e) {
            System.out.println("Exception in SelectorThread: " + e);
        } finally {
            if (st.isRunning()) {
                st.stopEndpoint();
            }
        }
    }
}
