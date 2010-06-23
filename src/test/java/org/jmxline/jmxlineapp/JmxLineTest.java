package org.jmxline.jmxlineapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.jmxline.app.JmxLine;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit test for simple App.
 */
public class JmxLineTest {

    private static final int port = 8080;
    private static final String host = "localhost";
    private static SimpleMBeanServer testServer = null;
    private static final String testName = "org.jmxline.jmxlinepapp:type=JmxLine";

    private final static Logger logger = LoggerFactory.getLogger(JmxLineTest.class);

    @BeforeClass
    public static void setup() {
        testServer = new SimpleMBeanServer();

        try {
            Object mbean = new SimpleMXBeanImpl(); 
            testServer.getServerInstance().registerMBean(mbean, new ObjectName(testName));
            logger.debug("Created " + testName);          
        } catch (MalformedObjectNameException e) {
            logger.error("MalformedObjectNameException",e);
        } catch (NullPointerException e) {
            logger.error("NullPointerException",e);
        } catch (InstanceAlreadyExistsException e) {
            logger.error("InstanceAlreadyExistsException",e);
        } catch (MBeanRegistrationException e) {
            logger.error("MBeanRegistrationException",e);
        } catch (NotCompliantMBeanException e) {
            logger.error("NotCompliantMBeanException",e);
        }
    }

    @AfterClass
    public static void teardown() {
        testServer = new SimpleMBeanServer();
    }

    @Test
    public void testConnection() {
        JmxLine line = new JmxLine(host, port);
        assertNotNull("Connection should not be null", line.getJmxConnection());
    }

    @Test
    public void testContainsBeanName() {
        JmxLine line = new JmxLine(host, port);
        List<String> names = line.getNameList();
        assertTrue("Should contain test name", names.contains(testName));
    }

    @Test
    public void retrieveName() {
        JmxLine line = new JmxLine(host, port);
        String aName = line.nameExists(testName);
        assertEquals(testName, aName);
    }

    @Ignore
    @Test
    public void retrieveAllAtts() {
        JmxLine line = new JmxLine(host, port);
        String name = "org.apache.cassandra.db:type=Caches,keyspace=MessageArchiver,cache=metaRowCache";
        System.out.println("+ " + name);
        line.printAttributes(name);
    }

    @Ignore
    @Test
    public void retrieveAtt() {
        JmxLine line = new JmxLine(host, port);
        String name = "org.apache.cassandra.db:type=Caches,keyspace=MessageArchiver,cache=metaRowCache";
        String attribute = "Size";
        System.out.println("+ " + name);
        String value = line.getAttribute(name, attribute);
        System.out.println("- " + attribute + ": " + value);
        assertNotNull(value);
    }

    @Test
    @Ignore
    public void httpTest() {

        String name = "org.apache.cassandra.db:type=Caches,keyspace=MessageArchiver,cache=metaRowCache";
        String attribute = "Size";

        try {
            HttpURLConnection con = (HttpURLConnection) new URL("http://localhost:8181/" + name + "/" + attribute)
                    .openConnection();
            con.setRequestMethod("GET");

            con.setConnectTimeout(5000);
            assertEquals(con.getResponseCode(), 200);
            assertEquals(con.getResponseMessage(), "");

            System.out.println("Response code: " + con.getResponseCode());
            System.out.println("Response msg: " + con.getResponseMessage());

        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
