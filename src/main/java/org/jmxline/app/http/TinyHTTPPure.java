package org.jmxline.app.http;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

import org.jmxline.app.JmxLine;

public class TinyHTTPPure extends Thread {
    private final Socket c;
    private final JmxLine line;
    
    public TinyHTTPPure(Socket s) {
        line = new JmxLine("c1", 8080);        
        c = s;
        start();
    }

    public static void main(String[] a) {
       
    }

    /**
     * GET /java.lang:type=ClassLoading/PeakThreadCount
     */
    public void run() {
        try {
            BufferedReader i = new BufferedReader(new InputStreamReader(c.getInputStream()));
            DataOutputStream o = new DataOutputStream(c.getOutputStream());
            try {
                String s, path;
                while ((s = i.readLine()).length() > 0) {
                    
                    // Grab the object name then the attribute
                    
                    if (s.startsWith("GE")) {
                        path = (s.split(" "))[1];
                        
                        // Remove leading /
                        path = path.replaceFirst("/", "");
                        
                        String[] pathBits = path.split("/");
                        String val = line.getAttribute(pathBits[0], pathBits[1]);
                                                
                        o.writeBytes("HTTP/1.0 200 OK\n" + "Content-Type: text/plain\n" + val + "\n\n");
                    }
                }
            } catch (Exception e) {
                o.writeBytes("HTTP/1.0 404 ERROR\n\n");
            }
            o.close();
        } catch (Exception e) {
        }
    }
}
