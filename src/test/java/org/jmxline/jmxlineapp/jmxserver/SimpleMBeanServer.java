package org.jmxline.jmxlineapp.jmxserver;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleMBeanServer {

    private final static Logger logger = LoggerFactory.getLogger(SimpleMBeanServer.class);
    private MBeanServer server;
    
    public SimpleMBeanServer(String host, int port) {

        try {

            Registry rmiRegistry = LocateRegistry.createRegistry(port);
            logger.info("Created RMI registry " + rmiRegistry.toString());
            
            server = MBeanServerFactory.createMBeanServer();
            
            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://" + host + ":" + port + "/jndi/rmi://" + host + ":" + port + "/jmxrmi");
            
            HashMap<String,Object> env = new HashMap<String,Object>();
            
            JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(url, env, server);
            cs.start();
            
            logger.info("JMXServer started at: " + cs.getAddress() + "\n");                       
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public MBeanServer getServerInstance() {
        return server;
    }
    
    public static void main(String[] args) {
        new SimpleMBeanServer("localhost", 9999);
    }
}
