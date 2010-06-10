package org.jmxline.jmxlineapp;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;

import com.sun.jdmk.comm.HtmlAdaptorServer;

public class SimpleMBeanServer {

    private final int rmiPort = 1099;
    private final int jmxPort = 9999;
    private JMXConnectorServer connector;
    private HtmlAdaptorServer adapter;

    public SimpleMBeanServer() {
        
    }

    public void createServer() {
        try {

            // Check we need this?
            System.setProperty("com.sun.management.jmxremote.authenticate", "false");

            // Create the RMI registry
            LocateRegistry.createRegistry(rmiPort);

            // Create the MBean server
            MBeanServer server = MBeanServerFactory.createMBeanServer("test");
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("java.naming.factory.initial", "com.sun.jndi.rmi.registry.RegistryContextFactory");
            map.put("java.naming.provider.url", "rmi://127.0.0.1:" + rmiPort);
            map.put(RMIConnectorServer.JNDI_REBIND_ATTRIBUTE, "true");

            JMXServiceURL url = new JMXServiceURL("rmi", "127.0.0.1", jmxPort, "/jndi/test");
            connector = JMXConnectorServerFactory.newJMXConnectorServer(url, map, server);

            // Register an mbean
            // server.registerMBean(connector, new
            // ObjectName("system:name=testmbean"));

            adapter = new HtmlAdaptorServer();
            ObjectName httpName = new ObjectName("system:name=http");
            server.registerMBean(adapter, httpName);
            adapter.setPort(9292);

            System.out.println("MBean Server started.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
//        TestMBeanServer testServer = new TestMBeanServer();
//        testServer.startServices();
    }

    public void startServices() {
        try {
            connector.start();
            adapter.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void stopServices() {
        try {
            connector.stop();
            adapter.stop();
            System.out.println("MBean Server stopped.");
        } catch (IOException e) {         
            e.printStackTrace();
        } 
    }
}
