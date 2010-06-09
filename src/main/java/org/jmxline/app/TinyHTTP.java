package org.jmxline.app;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class TinyHTTP extends Thread {
    private final Socket c;
    private final JmxLine line;
    
    public TinyHTTP(Socket s) {
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
