package org.jmxline.app;

import java.io.IOException;
import java.net.MalformedURLException;
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

/**
 * Simple Jmx command line app.
 */
public class JmxLine {

    private MBeanServerConnection mbs;

    public JmxLine(String host, int port) {

        JMXServiceURL serviceURL;
        try {
            serviceURL = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi");

            JMXConnector connector = JMXConnectorFactory.connect(serviceURL, null);
            mbs = connector.getMBeanServerConnection();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        // Default properties
        String host = "localhost";
        int port = 8081;

        if (args.length < 1) {
            usage();
        }

        if (args[0] != null) {
            
            // Horrible, horrible
            if (args[0].equals("server")) {
                startServerInForeground();
                System.exit(0);
            }
            
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

    private static void startServerInForeground() {
        Runnable runnableServer = new Runnable() {
            
            @Override
            public void run() {
                JmxLineAgent.startServer();                
            }
        };
        Thread serverThread = new Thread(runnableServer);
        serverThread.setDaemon(false);
        serverThread.start();
        while(true) {
            // Spin...
        }
    }

    private static void usage() {
        System.out.println("Usage: jmxline <hostname>:<port>");
        System.exit(-1);
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

}
