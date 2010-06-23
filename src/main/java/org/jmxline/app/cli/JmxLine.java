package org.jmxline.app.cli;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

import org.jmxline.app.httpserver.HttpServerWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main JMXLine class.
 */
public class JmxLine {

    private final static Logger logger = LoggerFactory.getLogger(JmxLine.class);

    private String host;
    private int port;
    private String name;

    /**
     * JMXLine constructor.
     * 
     * @param host
     *            is the JMX server hostname
     * @param port
     *            is the JMX server port number
     */
    public JmxLine(String host, int port, String name) {

        this.host = host;
        this.port = port;
        this.name = name;
    }

    public JmxLine(String host, int port) {
        this(host, port, null);
    }

    private JMXConnector getConnection() {

        try {

            String nameToUse = name != null ? name : "jmxrmi";

            JMXServiceURL serviceURL = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/"
                    + nameToUse);

            logger.info("Created server with service URL: " + serviceURL);
            
            return JMXConnectorFactory.connect(serviceURL, null);            

        } catch (IOException e) {
            logger.error("Could not connect via JMX " + host + ":" + port + "\n" + e);
        }
        return null;
    }

    /**
     * Start a webserver up to interpret HTTP requests.
     */
    private static void startStandloneServer() {

        HttpServerWrapper.startServer();

        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
        }

    }

    public JMXConnector getJmxConnection() {
        return getConnection();
    }

    public void getCount() {
        JMXConnector connector = getConnection();
        try {
            Integer count = connector.getMBeanServerConnection().getMBeanCount();
            logger.info("Count: " + count);
        } catch (IOException e) {
            logger.error("IOExcepton ", e);
        } finally {
            closeConnection(connector);
        }
    }

    public void getDomains() {
        JMXConnector connector = getConnection();
        try {
            String[] domains = connector.getMBeanServerConnection().getDomains();
            for (String domain : domains) {
                logger.info("Domain " + domain);
            }
        } catch (IOException e) {
            logger.error("IOExcepton ", e);
        } finally {
            closeConnection(connector);
        }
    }

    public void printNames() {
        for (String name : getNameList()) {
            logger.info(name);
        }
    }

    public List<String> getNameList() {
        List<String> names = new ArrayList<String>();
        JMXConnector connector = getConnection();
        Set<ObjectInstance> beans;
        try {
            beans = connector.getMBeanServerConnection().queryMBeans(null, null);

            for (ObjectInstance instance : beans) {
                names.add(instance.getObjectName().toString());
            }
        } catch (IOException e) {
            logger.error("IOExcepton ", e);
        } finally {
            closeConnection(connector);
        }
        return names;
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
            logger.error("IOExcepton ", e);
        } finally {
            closeConnection(connector);
        }

        return null;
    }

    private void closeConnection(JMXConnector connector) {
        try {
            connector.close();
        } catch (IOException e) {
            try {
                logger.info("Could not close connection " + connector.getConnectionId());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void printAttributes(String name) {
        for (String attribute : getAttributeList(name)) {
            logger.info(attribute);
        }
    }

    public List<String> getAttributeList(String name) {
        List<String> attributes = new ArrayList<String>();
        JMXConnector connector = getConnection();
        ObjectName oName = null;
        if (name != null) {
            oName = createJmxObject(name);
        }

        try {
            MBeanInfo info = connector.getMBeanServerConnection().getMBeanInfo(oName);
            for (MBeanAttributeInfo att : info.getAttributes()) {
                attributes.add(" - " + att.getName() + " [" + att.getType() + "] " + att.getDescription());
            }

        } catch (ReflectionException e) {
            logger.error("ReflectionException ", e);
        } catch (IOException e) {
            logger.error("IOExcepton ", e);
        } catch (InstanceNotFoundException e) {
            logger.error("InstanceNotFoundException ", e);
        } catch (IntrospectionException e) {
            logger.error("IntrospectionException ", e);
        } finally {
            closeConnection(connector);
        }
        return attributes;
    }

    public String getAttribute(String name, String attribute) {
        JMXConnector connector = getConnection();
        try {
            ObjectName obj = createJmxObject(name);
            return connector.getMBeanServerConnection().getAttribute(obj, attribute).toString();

        } catch (InstanceNotFoundException e) {
            logger.error("InstanceNotFoundException ", e);
        } catch (ReflectionException e) {
            logger.error("ReflectionException ", e);
        } catch (IOException e) {
            logger.error("IOException ", e);
        } catch (AttributeNotFoundException e) {
            logger.error("AttributeNotFoundException ", e);
        } catch (MBeanException e) {
            logger.error("MBeanException ", e);
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
            logger.error("MalformedObjectNameException ", e);
        }
        return oName;
    }

    private static void usage() {
        logger.info("Usage: java -jar jmxline-app.jar <hostname>:<port> \n"
                + "or... java -jar jmxline-app.jar standalone");
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

        // TODO: Change to commons-cli
        if (args.length == 1) {
            logger.info("No object name specified, current list for " + host + ":" + port);
            jmxLine.printNames();
        }

        if (args.length == 2) {
            // No attribute specified, print all
            logger.info("No attribute specified, current attributes for " + host + ":" + port + " and " + args[1]);
            jmxLine.printAttributes(args[1]);
        }

        if (args.length == 3) {

            if (args[1] != null && args[2] == null) {
                logger.info("No attribute specified, current names for" + host + ":" + port);
                jmxLine.printAttributes(args[1]);
            }

            // get value of attribute
            if (args[1] != null && args[2] != null) {
                String val = jmxLine.getAttribute(args[1], args[2]);
                logger.info("- " + args[2] + ": " + val);
                logger.info("+ " + args[1]);
            }
        }
    }

}