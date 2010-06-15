package org.jmxline.app;

import java.io.IOException;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.jmxline.app.http.HttpServerWrapper;

public class JmxLine {

    private String host;
    private int port;

    /**
     * JMXLine constructor.
     * 
     * @param host
     *            is the JMX server hostname
     * @param port
     *            is the JMX server port number
     */
    public JmxLine(String host, int port) {

        this.host = host;
        this.port = port;
    }

    private JMXConnector getConnection() {
        JMXConnector connector = null;

        try {
            JMXServiceURL serviceURL = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + port
                    + "/jmxrmi");
            connector = JMXConnectorFactory.connect(serviceURL, null);
            return connector;
        } catch (IOException e) {
            System.out.println("Could not connect via JMX " + host + ":" + port + "\n" + e);
        }
        return null;
    }

    /**
     * Start a webserver up to interpret HTTP requests.
     */
    private static void startStandloneServer() {

        HttpServerWrapper.startServer();
       
    }

    public void getCount() {
        JMXConnector connector = getConnection();        
        try {
            Integer count = connector.getMBeanServerConnection().getMBeanCount();
            System.out.println("Count: " + count);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeConnection(connector);
        }
    }

    public void getDomains() {
        JMXConnector connector = getConnection();
        try {
            String[] domains = connector.getMBeanServerConnection().getDomains();
            for (String domain : domains) {
                System.out.println("Domain " + domain);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeConnection(connector);
        }
    }

    public void printNames() {
        JMXConnector connector = getConnection();
        Set<ObjectInstance> beans;
        try {
            beans = connector.getMBeanServerConnection().queryMBeans(null, null);

            for (ObjectInstance instance : beans) {
                System.out.println(" + " + instance.getObjectName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeConnection(connector);
        }
    }

    public String nameExists(String name) {

        JMXConnector connector = getConnection();
        ObjectName oName = null;
        if (name != null) {
            oName = createJmxObject(name);
        }

        Set<ObjectInstance> beans;
        try {
            beans = connector.getMBeanServerConnection().queryMBeans(oName, null);

            for (ObjectInstance instance : beans) {
                return instance.getObjectName().toString();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeConnection(connector);
        }

        return null;
    }

    private void closeConnection(JMXConnector connector) {
        try {
            connector.close();
        } catch (IOException e) {
            System.out.println("Could not close connection.");
            e.printStackTrace();
        }
    }

    public void printAttributes(String name) {
        JMXConnector connector = getConnection();
        ObjectName oName = null;
        if (name != null) {
            oName = createJmxObject(name);
        }

        try {
            MBeanInfo info = connector.getMBeanServerConnection().getMBeanInfo(oName);
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
        } finally {
            closeConnection(connector);
        }
    }

    public String getAttribute(String name, String attribute) {
        JMXConnector connector = getConnection();
        try {
            ObjectName obj = createJmxObject(name);
            return connector.getMBeanServerConnection().getAttribute(obj, attribute).toString();

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
        } finally {
            closeConnection(connector);
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
     * Main. Change this to use Commons-CLI.
     * 
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