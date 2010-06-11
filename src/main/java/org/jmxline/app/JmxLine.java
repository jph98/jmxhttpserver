package org.jmxline.app;

import java.io.IOException;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.jmxline.app.http.HttpServerWrapper;

public class JmxLine {

    private MBeanServerConnection mbs;   

    /**
     * JMXLine constructor.
     * 
     * @param host is the JMX server hostname 
     * @param port is the JMX server port number
     */
    public JmxLine(String host, int port) {

        try {
            JMXServiceURL serviceURL = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi");                           
            JMXConnector connector = JMXConnectorFactory.connect(serviceURL, null);
            mbs = connector.getMBeanServerConnection();
        } catch (IOException e) {
            System.out.println("Could not connect via JMX " + host + ":" + port + "\n" + e);
        }
    }
    
    public boolean isConnected() {
        return mbs != null;
    }

    /**
     * Start a webserver up to interpret HTTP requests.
     */
    private static void startStandloneServer() {
        
        HttpServerWrapper.startServer();
        
        try {
            System.out.println("zzz...");
            Thread.sleep(Integer.MAX_VALUE);            
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
            
    }

    public void getCount() throws IOException {
        Integer count = mbs.getMBeanCount();
        System.out.println("Count: " + count);
    }

    public void getDomains() throws IOException {
        String[] domains = mbs.getDomains();
        for (String domain : domains) {
            System.out.println("Domain " + domain);
        }
    }

    public void printNames() {
        Set<ObjectInstance> beans;
        try {
            beans = mbs.queryMBeans(null, null);

            for (ObjectInstance instance : beans) {
                System.out.println(" + " + instance.getObjectName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String nameExists(String name) {

        ObjectName oName = null;
        if (name != null) {
            oName = createJmxObject(name);
        }

        Set<ObjectInstance> beans;
        try {
            beans = mbs.queryMBeans(oName, null);

            for (ObjectInstance instance : beans) {
                return instance.getObjectName().toString();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void printAttributes(String name) {
        ObjectName oName = null;
        if (name != null) {
            oName = createJmxObject(name);
        }

        try {
            MBeanInfo info = mbs.getMBeanInfo(oName);
            for (MBeanAttributeInfo att : info.getAttributes()) {
                System.out.println(" - " + att.getName() + " [" + att.getType() + "] " + att.getDescription());
            }

        } catch (ReflectionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }
    }

    public String getAttribute(String name, String attribute) {
        try {
            return mbs.getAttribute(createJmxObject(name), attribute).toString();
        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
        } catch (ReflectionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AttributeNotFoundException e) {
            e.printStackTrace();
        } catch (MBeanException e) {
            e.printStackTrace();
        }

        return null;
    }

    private ObjectName createJmxObject(String aName) {
        ObjectName oName = null;
        try {
            oName = new ObjectName(aName);
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return oName;
    }

    private static void usage() {
        System.out
                .println("Usage: java -jar jmxline-app.jar <hostname>:<port> \nor... java -jar jmxline-app.jar standalone");
        System.exit(-1);
    }

    /**
     * Main.
     * Change this to use Commons-CLI.
     * @param args
     */
    public static void main(String[] args) {

        // Default properties
        String host = "localhost";
        int port = 8282;

        if (args.length < 1) {
            usage();
        }

        if (args[0] != null) {

            // Horrible, horrible
            if (args[0].equals("standalone")) {
                startStandloneServer();
                System.exit(0);
            }

            // Else look for the host:port combination
            String[] parts = args[0].split(":");
            host = parts[0];
            port = Integer.parseInt(parts[1]);
        }

        JmxLine jmxLine = new JmxLine(host, port);

        if (jmxLine.isConnected()) {
            
            if (args.length == 1) {
                System.out.println("No object name specified, current list for " + host + ":" + port);
                jmxLine.printNames();
            }
    
            if (args.length == 2) {
                // No attribute specified, print all
                System.out.println("No attribute specified, current attributes for " + host + ":" + port + " and "
                        + args[1]);
                jmxLine.printAttributes(args[1]);
            }
    
            if (args.length == 3) {
    
                if (args[1] != null && args[2] == null) {
                    System.out.println("No attribute specified, current names for" + host + ":" + port);
                    jmxLine.printAttributes(args[1]);
                }
    
                // get value of attribute
                if (args[1] != null && args[2] != null) {
                    String val = jmxLine.getAttribute(args[1], args[2]);
                    System.out.println("+ " + args[1]);
                    System.out.println("- " + args[2] + ": " + val);
                }
            }
        }
    }

}