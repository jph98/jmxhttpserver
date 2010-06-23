package org.jmxline.jmxlineapp;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleMBeanServer {

	private final static Logger logger = LoggerFactory.getLogger(SimpleMBeanServer.class);
	
    private MBeanServer server;

    public SimpleMBeanServer() {
        try {
            server = ManagementFactory.getPlatformMBeanServer();            
            logger.info("Server: " + server.toString());
        } catch (Exception e) {
            logger.error("IOException ", e);
        }
    }
    
    public MBeanServer getServerInstance() {
        return server;
    }
         
}
