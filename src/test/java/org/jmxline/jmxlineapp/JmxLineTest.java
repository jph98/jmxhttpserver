package org.jmxline.jmxlineapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import org.jmxline.app.JmxLine;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class JmxLineTest {

    private static final int port = 8080;
    private static final String host = "localhost";
    private static SimpleMBeanServer testServer = null;
    
    @BeforeClass
    public static void setup() {
//         testServer = new SimpleMBeanServer();
//         testServer.createServer();
//         testServer.startServices();
    }
    
    @AfterClass
    public static void teardown() {
//         testServer = new SimpleMBeanServer();
//         testServer.stopServices();
    }
    
    @Ignore
    @Test
    public void testConnection() {
    	JmxLine line = new JmxLine(host, port);
    	assertNotNull("Connection should not be null", line.getJmxConnection());
    }
    
    @Ignore
    @Test
    public void retrieveAllNames() {
        JmxLine line = new JmxLine(host, port);
        line.printNames();
    }
    
    @Ignore
    @Test
    public void retrieveName() {
        JmxLine line = new JmxLine(host, port);
        String name = "system:name=testmbean";
        String eName = line.nameExists(name);
        assertEquals(name, eName);
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
