package org.jmxline.app.http;

import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.util.Enumeration;

import org.jmxline.app.JmxLine;

import com.sun.grizzly.http.SelectorThread;
import com.sun.grizzly.tcp.Adapter;
import com.sun.grizzly.tcp.OutputBuffer;
import com.sun.grizzly.tcp.Request;
import com.sun.grizzly.tcp.Response;
import com.sun.grizzly.util.buf.ByteChunk;
import com.sun.grizzly.util.http.Parameters;

public class GrizzlyHttp implements Adapter {

    public GrizzlyHttp() {
    }

    public void service(Request request, Response response) throws Exception {
        String requestURI = request.requestURI().toString();

        System.out.println("New incoming request with URI: " + requestURI);

        if (request.method().toString().equalsIgnoreCase("GET")) {
            response.setStatus(HttpURLConnection.HTTP_OK);
            
            JmxLine line = new JmxLine("c1", 8080);
            
            requestURI = requestURI.replaceFirst("/", "");
            String[] parts = requestURI.split("/");                       
            
            byte[] bytes = null;
                
            if (parts.length == 1) {
                bytes = "No attribute name specified".getBytes();
            }
            
            if (parts.length == 2) {
                try { 
                    String name = URLDecoder.decode(parts[0], "UTF-8");
                    String attr = URLDecoder.decode(parts[1], "UTF-8");
                    System.out.println("Name " + name + " attr " + attr);
                    String val = line.getAttribute(name, attr);                    
                    
                    bytes = val.getBytes();
                } catch (Exception e) {
                    bytes = "Error getting property".getBytes();
                }                
            }
            
            ByteChunk chunk = new ByteChunk();
            response.setContentLength(bytes.length);
            response.setContentType("text/plain");
            chunk.append(bytes, 0, bytes.length);
            OutputBuffer buffer = response.getOutputBuffer();
            buffer.doWrite(chunk, response);
            response.finish();
        }
    }

    public void afterService(Request request, Response response) throws Exception {
        request.recycle();
        response.recycle();
    }

    public void fireAdapterEvent(String string, Object object) {
    }

    public static void main(String[] args) {

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