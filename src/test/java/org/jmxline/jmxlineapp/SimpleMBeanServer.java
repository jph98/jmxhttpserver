package org.jmxline.jmxlineapp;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.HashMap;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.rmi.RMIConnectorServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleMBeanServer {

	private final static Logger logger = LoggerFactory.getLogger(SimpleMBeanServer.class);
	
    private final int rmiPort = 1099;
    
    private JMXConnectorServer connector;
    //private HtmlAdaptorServer adapter;

    public SimpleMBeanServer() {
        
    }

    public void createServer() {
        try {

        	final MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
        	
        	// Gargh!!!
    		mbeanServer.createMBean("system:name=testmbean", new ObjectName("system:name=testmbean"));	

    		final JmxServer jmxServer = new JmxServer(InetAddress.getByName("localhost"));
    		jmxServer.start();	
            
        } catch (Exception e) {
            logger.error("IOException ", e);
        }
    }

    private void addHttpMBean() {
//      adapter = new HtmlAdaptorServer();
//      ObjectName httpName = new ObjectName("system:name=http");
//      server.registerMBean(adapter, httpName);
//      adapter.setPort(9292);

    }
    
	private HashMap<String, String> getJmxPropMap() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("java.naming.factory.initial", "com.sun.jndi.rmi.registry.RegistryContextFactory");
		map.put("java.naming.provider.url", "rmi://localhost:" + rmiPort);
		map.put(RMIConnectorServer.JNDI_REBIND_ATTRIBUTE, "true");
		return map;
	}

    public void startServices() {
        try {
            connector.start();
            //adapter.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void stopServices() {
        try {
            connector.stop();
            //adapter.stop();
            System.out.println("MBean Server stopped.");
        } catch (IOException e) {         
            e.printStackTrace();
        } 
    }
}
