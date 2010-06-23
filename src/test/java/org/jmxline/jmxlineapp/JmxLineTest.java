package org.jmxline.jmxlineapp;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
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

import org.jmxline.app.cli.JmxLine;
import org.jmxline.app.httpserver.HttpServerWrapper;
import org.jmxline.jmxlineapp.jmxserver.SimpleMBeanServer;
import org.jmxline.jmxlineapp.jmxserver.SimpleMXBeanImpl;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit test for simple App.
 */
public class JmxLineTest {

    private static final int port = 9999;
    private static final String host = "localhost";
    private static SimpleMBeanServer testServer = null;
    private static final String testName = "org.jmxline.jmxlinepapp:type=JmxLine";
    private static final String testAttribute = "Used [long] Used";

    private final static Logger logger = LoggerFactory.getLogger(JmxLineTest.class);

    @BeforeClass
    public static void setup() {
        testServer = new SimpleMBeanServer(host, port);

        registerBean(new SimpleMXBeanImpl(), testName);
    }

    private static void registerBean(Object object, String name) {
        try {
            
            ObjectName objectName = new ObjectName(testName);
            testServer.getServerInstance().registerMBean(object, objectName);            
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

    @Test
    public void testConnection() {
        JmxLine line = new JmxLine(host, port, null);
        assertNotNull("Connection should not be null", line.getJmxConnection());
    }

    @Test
    public void testDebugNames() {
        JmxLine line = new JmxLine(host, port);
        for (String name: line.getNameList()) {
            logger.info("Name: " + name);
        }
        
    }
    @Test
    public void testContainsBeanName() {
        JmxLine line = new JmxLine(host, port);
        List<String> names = line.getNameList();
        logger.info("Found " + names.contains(testName));
        assertTrue("Should contain test name", names.contains(testName));
    }

    @Test
    public void retrieveBeanName() {
        JmxLine line = new JmxLine(host, port);
        String name = line.nameExists(testName);
        assertThat(testName, is(equalTo(name)));
    }      
    
    @Test
    public void retrieveAttributeValue() {
        JmxLine line = new JmxLine(host, port);        
        String attribute = "Used";        
        String value = line.getAttribute(testName, attribute);
        assertThat(value, is(equalTo(value)));
    }

    @Ignore
    @Test    
    public void retrieveAttributeValueViaHttp() {              

        HttpServerWrapper.startServer(HttpServerWrapper.DEFAULT_HTTP_PORT, HttpServerWrapper.DEFAULT_JMX_HOST, HttpServerWrapper.DEFAULT_JMX_PORT);
        
        try {
            
            String url = "http://" + HttpServerWrapper.DEFAULT_HTTP_HOST + ":" + HttpServerWrapper.DEFAULT_HTTP_PORT;            
            HttpURLConnection con = (HttpURLConnection) new URL(url + "/" + testName + "/" + testAttribute)
                                                        .openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(5000);
            
            assertThat(con.getResponseCode(), is(equalTo(200)));            

            System.out.println("Response code: " + con.getResponseCode());
            System.out.println("Response msg: " + con.getResponseMessage());

        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
