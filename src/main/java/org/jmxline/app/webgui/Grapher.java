package org.jmxline.app.webgui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jmxline.app.httpserver.HttpServerWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Grapher {
    
    private static final Logger log = LoggerFactory.getLogger(HttpServerWrapper.class);
    
    private static final int INTERVAL = 1000;

    public Grapher() {
    
        Runnable run = new Runnable() {
            
            @Override
            public void run() {
                    
                String configText = "";
                InputStream resourceAsStream = getClass().getResourceAsStream("params.config");
                try {
                    configText = IOUtils.toString(resourceAsStream);
                    log.info(configText);
                } catch (IOException e) {
                    log.error("IOException", e);
                }
                
                // For each item eventually
                
                String urlText = "http://localhost:8080/" + configText;
                try {
                    log.info("Connecting to " + urlText);
                    
                    HttpURLConnection con = (HttpURLConnection) new URL(urlText).openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(1000);                    
                    con.connect();
                    
                    if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        
                        InputStream inStream = con.getInputStream();
                        String result = IOUtils.toString(inStream);
                        log.info("Result " + result);
                    }
                    
                } catch (MalformedURLException mue) {
                    log.error("MalformedURLException problem connecting", mue);
                } catch (IOException ioe) {
                    log.error("IOException Problem connecting", ioe);
                }
                
                try {
                    Thread.sleep(INTERVAL);
                } catch (InterruptedException e) {
                    // Do nothing
                }
            }
        };
        
        Thread graphThread = new Thread(run);
        graphThread.start();
        
    }
    
    public static void main(String[] args) {
        
        Grapher grapher = new Grapher();
    }
}
