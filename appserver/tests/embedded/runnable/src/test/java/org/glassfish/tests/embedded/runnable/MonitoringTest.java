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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.management.JMException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.glassfish.tests.embedded.runnable.TestArgumentProviders.GfEmbeddedJarNameProvider;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static java.util.logging.Level.WARNING;
import static org.glassfish.tests.embedded.runnable.GfEmbeddedUtils.runGlassFishEmbedded;
import static org.glassfish.tests.embedded.runnable.ShrinkwrapUtils.logArchiveContent;
import static org.glassfish.tests.embedded.runnable.TestUtils.waitFor;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * @author Ondro Mihalyi
 */
public class MonitoringTest {

    private static final Logger LOG = Logger.getLogger(MonitoringTest.class.getName());

    @ParameterizedTest
    @ArgumentsSource(GfEmbeddedJarNameProvider.class)
    void testJmxMonitoringWithFlashlightAgent(String gfEmbeddedJarName) throws Exception {
        assumeTrue(!gfEmbeddedJarName.endsWith("web.jar"),
                "AMX is not supported by glassfish-embedded-web.jar, skipping this test scenario");
        Process gfEmbeddedProcess = null;
        JMXConnector jmxConnector = null;
        File warFile = null;
        try {
            // an app needs to be deployed to initialize request monitoring
            warFile = createEmptyApp();
            gfEmbeddedProcess = startGlassFishWithJmx(gfEmbeddedJarName, warFile);

            jmxConnector = connectToJmx();
            MBeanServerConnection mbsc = jmxConnector.getMBeanServerConnection();

            bootAmx(mbsc);

            verifyMonitoringMBeans(mbsc);

        } finally {
            cleanup(jmxConnector, gfEmbeddedProcess, warFile);
        }
    }

    private File createEmptyApp() throws Exception {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "empty-app.war")
                .addAsWebInfResource("", "beans.xml");

        File warFile = File.createTempFile("empty-app", ".war");
        war.as(ZipExporter.class).exportTo(warFile, true);
        logArchiveContent(war, "empty-app.war", LOG::info);
        return warFile;
    }

    private Process startGlassFishWithJmx(String gfEmbeddedJarName, File warFile) throws IOException {
        return runGlassFishEmbedded(gfEmbeddedJarName, true,
                List.of("-Dcom.sun.management.jmxremote",
                        "-javaagent:flashlight-agent.jar"),
                "enable-monitoring --modules http-service",
                warFile.getAbsolutePath());
    }

    private JMXConnector connectToJmx() throws InterruptedException {
        return waitFor("JMX connector", () -> {
            VirtualMachine vm = null;
            String connectorAddress = null;
            try {
                // Find GlassFish process by looking for our jar
                for (VirtualMachineDescriptor vmd : VirtualMachine.list()) {
                    if (vmd.displayName().contains("glassfish-embedded")) {
                        vm = VirtualMachine.attach(vmd.id());

                        // Get or create JMX connector address
                        Properties props = vm.getAgentProperties();
                        vm.detach();
                        vm = null;
                        connectorAddress = props.getProperty("com.sun.management.jmxremote.localConnectorAddress");

                        if (connectorAddress != null) {
                            JMXServiceURL serviceURL = new JMXServiceURL(connectorAddress);
                            return JMXConnectorFactory.connect(serviceURL, null);
                        } else {
                            throw new UnsupportedOperationException("Connector address not available!");
                        }
                    }
                }
            } catch (Exception e) {
                LOG.log(WARNING, e.getMessage(), e);
                if (vm != null) try { vm.detach(); } catch (Exception ignored) {}
            }
            return (JMXConnector)null;
        });
    }

    private void bootAmx(MBeanServerConnection mbsc) throws Exception {
        ObjectName bootAMXObjectName = new ObjectName("amx-support:type=boot-amx");

        waitFor("bootAMX", () -> mbsc.isRegistered(bootAMXObjectName) ? true : null);

        mbsc.invoke(bootAMXObjectName, "bootAMX", null, null);
    }

    private void verifyMonitoringMBeans(final MBeanServerConnection mbsc) throws InterruptedException, IOException, JMException {
        Set<ObjectName> requestBeans = waitFor("equest-mon bean", () -> {
            Set<ObjectName> result = mbsc.queryNames(new ObjectName("amx:type=request-mon,*"), null);
            return result.isEmpty() ? null : result;
        });

        // Verify we can read monitoring data
        ObjectName requestBean = requestBeans.iterator().next();
        assertNotNull(mbsc.getAttribute(requestBean, "countrequests"), "Should be able to read request count");
    }

    private void cleanup(JMXConnector jmxConnector, Process process, File... files) throws InterruptedException {
        if (jmxConnector != null) try { jmxConnector.close(); } catch (Exception ignored) {}
        if (process != null && process.isAlive()) {
            process.destroy();
            process.waitFor(5, TimeUnit.SECONDS);
            if (process.isAlive()) {
                process.destroy();
                process.waitFor(5, TimeUnit.SECONDS);
            }
        }
        cleanupFiles(files);
    }

    private void cleanupFiles(File... files) {
        for (File file : files) {
            Optional.ofNullable(file).ifPresent(File::delete);
        }
    }
}
