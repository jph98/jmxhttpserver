package org.jmxline.jmxlineapp;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import org.jmxline.app.JmxLine;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class JmxLineTest {

    @Test
    public void retrieveAllNames() {
        JmxLine line = new JmxLine("c1", 8080);
        line.printNames();
    }

    @Test
    public void retrieveName() {
        JmxLine line = new JmxLine("c1", 8080);
        String name = "org.apache.cassandra.db:type=Caches,keyspace=MessageArchiver,cache=metaRowCache";
        String eName = line.nameExists(name);
        assertEquals(name, eName);
    }

    @Test
    public void retrieveAllAtts() {
        JmxLine line = new JmxLine("c1", 8080);
        String name = "org.apache.cassandra.db:type=Caches,keyspace=MessageArchiver,cache=metaRowCache";
        System.out.println("+ " + name);
        line.printAttributes(name);
    }

    @Test
    public void retrieveAtt() {
        JmxLine line = new JmxLine("c1", 8080);
        String name = "org.apache.cassandra.db:type=Caches,keyspace=MessageArchiver,cache=metaRowCache";
        String attribute = "Size";
        System.out.println("+ " + name);
        String value = line.getAttribute(name, attribute);
        System.out.println("- " + attribute + ": " + value);
        assertNotNull(value);
    }

    @Test
    public void httpTest() {

        String name = "org.apache.cassandra.db:type=Caches,keyspace=MessageArchiver,cache=metaRowCache";
        String attribute = "Size";
        
        try {
            HttpURLConnection con = (HttpURLConnection) new URL("http://localhost:8181/" + name + "/" + attribute).openConnection();
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
