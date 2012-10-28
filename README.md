JMXLine
=======

JMXLine functions as:
 a) JMX command line console that can query an application via RMI
 b) simple web server that can be run standalone (or embedded as an agent in an application)

Building
--------

To build the agent, command line tool and standalone server use:

 mvn assembly:assembly
 
To setup for development with eclipse:

 mvn eclipse:eclipse

Setup
-----

1. COMMAND LINE MODE - will connect directly to the server via a JMXConnection

 java -jar jmxline-app.jar localhost:8080 <jmxobjectname> <jmxattribute>

* This will connect to the application running on service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi"
* If you do not specify <jmxobjectname> it will list all JMX objects for that host
* If you do not specify <jmxattribute> it will list all attributes for the specified <jmxobjectname>

e.g. java -jar target/jmxline-app.jar localhost:8080 java.lang:type=Threading PeakThreadCount


2. EMBEDDED AGENT MODE - By embedding jmxline-app.jar as an agent in a running application on the host machine.  You can then send HTTP GET requests for JMX attributes to the application.

Instrument your application with:

 -javaagent:jmxline-app.jar

This will start an embedded HTTP server (https://grizzly.dev.java.net/) up with your application. You can then query for a particular jmx attribute value:

 curl -XGET localhost:8282/java.lang:type=Threading/PeakThreadCount


3. STANDALONE WEBSERVER MODE - You can start jmxline-app.jar up as a standalone web server on the host machine.

 java -jar jmxline-app.jar standalone

Monitoring Application Configuration
------------------------------------

Zabbix is given an example here, but you should be able to use any monitoring program (Nagios/Ganglia/Munin) that can check/retrieve values via HTTP.

For Zabbix you need to create an external curl.sh script to connect to JMXLine:

 #!/bin/bash
 zabbixhostname=$1
 zabbixport=$2
 zabbixpath=$3
 curl -XGET $zabbixhostname:$zabbixport/$zabbixpath

Save this in /etc/zabbix/externalscripts.  Make sure that zabbix_server.conf has ExternalScripts set to this value.

Go to Configuration -> Hosts -> Items -> Create Item

Enter the following:
* Description: Peak Thread Count Check
* Type: External Check
* Key: curl.sh[8282 java.lang:type=Threading/PeakThreadCount]
* Type of Information: Text

This should poll the web server on the host machine which will return the attribute value.
