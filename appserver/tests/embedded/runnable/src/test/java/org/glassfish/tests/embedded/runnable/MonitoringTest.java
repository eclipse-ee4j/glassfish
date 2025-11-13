/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */
package org.glassfish.tests.embedded.runnable;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.lang.System.Logger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Properties;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.glassfish.tests.embedded.runnable.tool.EmbeddedGlassFishContainer;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.TRACE;
import static org.glassfish.tests.embedded.runnable.tool.BufferedReaderMatcher.readerContains;
import static org.glassfish.tests.embedded.runnable.tool.WaitFor.waitFor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Ondro Mihalyi
 * @author David Matejcek
 */
public class MonitoringTest {

    static final Logger LOG = System.getLogger(MonitoringTest.class.getName());

    @TempDir
    private Path tmpDir;
    private JMXConnector jmxConnector;
    private Path warFile;
    private EmbeddedGlassFishContainer glassfish;


    @BeforeEach
    void init() throws Exception {
        // an app needs to be deployed to initialize request monitoring
        warFile = createEmptyApp();
        glassfish = new EmbeddedGlassFishContainer("glassfish-embedded-all.jar", warFile);
        glassfish.start();
        assertThat(glassfish.getErrorReader(), readerContains("Application empty-app.war deployed at context root"));
    }

    @AfterEach
    void cleanup() throws Exception {
        if (jmxConnector != null) {
            LOG.log(INFO, () -> "Closing JMX Connector: " + jmxConnector);
            try {
                jmxConnector.close();
            } catch (Exception e) {
                LOG.log(ERROR, "JMX Connector close failed!", e);
            }
        }
        if (glassfish.isAlive()) {
            LOG.log(INFO, "Terminating the process...");
            glassfish.stop();
        }
        Files.deleteIfExists(warFile);
    }

    @Test
    void testJmxMonitoringWithFlashlightAgent() throws Exception {
        jmxConnector = getJMXConnector();
        LOG.log(INFO, () -> "Connecting with JMX Connector: " + jmxConnector);
        assertNotNull(jmxConnector, "JMX Connector");
        jmxConnector.connect();
        MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
        glassfish.startReadingStdErr();
        Object bootAmxResponse = bootAmx(connection);
        LOG.log(INFO, () -> "AMX booted: " + bootAmxResponse);
        assertNotNull(bootAmxResponse, "bootAMX operation");

        LOG.log(INFO, () -> "Retrieving monitor data using connection: " + connection);
        Set<ObjectName> requestBeans = waitFor(Duration.ofSeconds(10L), "The request-mon bean", () -> {
            Set<ObjectName> result = connection.queryNames(new ObjectName("amx:type=request-mon,*"), null);
            return result.isEmpty() ? null : result;
        });

        // Verify we can read monitoring data
        ObjectName requestBean = requestBeans.iterator().next();
        Object requestCount = connection.getAttribute(requestBean, "countrequests");
        LOG.log(INFO, () -> "Detected request count: " + requestCount);
        assertNotNull(requestCount, "Should be able to read request count");
    }

    private Path createEmptyApp() throws Exception {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "empty-app.war").addAsWebInfResource(EmptyAsset.INSTANCE,
            "beans.xml");
        Path file = tmpDir.resolve("empty-app.war");
        war.as(ZipExporter.class).exportTo(file.toFile(), true);
        LOG.log(INFO, () -> "Generated empty war file: " + war.toString(true));
        return file;
    }

    /**
     * Find GlassFish process by looking for our jar and create {@link JMXConnector}
     */
    private JMXConnector getJMXConnector() throws Exception {
        String connectorAddress = getJmxUrl();
        LOG.log(TRACE, () -> "JMX Connector address: " + connectorAddress);
        JMXServiceURL serviceURL = new JMXServiceURL(connectorAddress);
        LOG.log(INFO, () -> "JMX Service URL: " + serviceURL);
        return JMXConnectorFactory.newJMXConnector(serviceURL, null);
    }

    private String getJmxUrl() throws Exception {
        final VirtualMachineDescriptor vmd = findVirtualMachineDescriptor();
        final VirtualMachine vm = VirtualMachine.attach(vmd.id());
        try {
            Properties props = vm.getAgentProperties();
            String connectorAddress = props.getProperty("com.sun.management.jmxremote.localConnectorAddress");
            if (connectorAddress == null) {
                throw new UnsupportedOperationException("Connector address not available!");
            }
            // FIXME: Remote connections are not usually allowed, however for some reason JMX sometimes changed
            //        the 127.0.0.1 to host's public address. On my machine randomly in one of 5 executions.
            return connectorAddress.replaceFirst("[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}", "localhost");
        } finally {
            vm.detach();
        }
    }

    /**
     * Find GlassFish process by looking for our jar
     */
    private VirtualMachineDescriptor findVirtualMachineDescriptor() throws Exception {
        return waitFor(Duration.ofSeconds(10L), "Virtual machine descriptor", () -> {
            for (VirtualMachineDescriptor vmd : VirtualMachine.list()) {
                if (vmd.displayName().contains("glassfish-embedded")) {
                    return vmd;
                }
            }
            return null;
        });
    }

    private Object bootAmx(MBeanServerConnection connection) throws Exception {
        LOG.log(INFO, "Booting AMX!");
        ObjectName bootAMXObjectName = new ObjectName("amx-support:type=boot-amx");
        waitFor(Duration.ofSeconds(10L), "bootAMX registered",
            () -> connection.isRegistered(bootAMXObjectName) ? true : null);
        LOG.log(INFO, "Invoking bootAMX...");
        return waitFor(Duration.ofSeconds(10L), "Invoking bootAMX response", () -> {
            return connection.invoke(bootAMXObjectName, "bootAMX", null, null);
        });
    }
}
