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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.glassfish.tests.embedded.runnable.TestArgumentProviders.GfEmbeddedJarNameProvider;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Ondro Mihalyi
 */
public class MonitoringTest {

    private static final Logger LOG = Logger.getLogger(MonitoringTest.class.getName());
    private static final int JMX_PORT = 8686;
    private static final int WAIT_SECONDS = 30;

    @ParameterizedTest
    @ArgumentsSource(GfEmbeddedJarNameProvider.class)
    void testJmxMonitoringWithFlashlightAgent(String gfEmbeddedJarName) throws Exception {
        Process gfEmbeddedProcess = null;
        JMXConnector jmxConnector = null;
        try {
            gfEmbeddedProcess = startGlassFishWithJmx(gfEmbeddedJarName);

            jmxConnector = connectToJmx();
            MBeanServerConnection mbsc = jmxConnector.getMBeanServerConnection();

            bootAmxAndWait(mbsc);

            verifyMonitoringMBeans(mbsc);

        } finally {
            cleanup(jmxConnector, gfEmbeddedProcess);
        }
    }

    private File createMonitoringPropertiesFile() throws Exception {
        File propertiesFile = File.createTempFile("monitoring", ".properties");
        java.nio.file.Files.write(propertiesFile.toPath(), List.of(
                "configs.config.server-config.monitoring-service.module-monitoring-levels.http-service=HIGH",
                "configs.config.server-config.monitoring-service.module-monitoring-levels.thread-pool=HIGH"
        ));
        return propertiesFile;
    }

    private Process startGlassFishWithJmx(String gfEmbeddedJarName) throws IOException {
        List<String> arguments = new ArrayList<>();
        arguments.add(ProcessHandle.current().info().command().get());
        arguments.addAll(List.of(
                "-javaagent:flashlight-agent.jar",
                "-Dcom.sun.management.jmxremote",
                "-Dcom.sun.management.jmxremote.port=" + JMX_PORT,
                "-Dcom.sun.management.jmxremote.authenticate=false",
                "-Dcom.sun.management.jmxremote.ssl=false",
                "-jar", gfEmbeddedJarName,
                "--noPort",
                "enable-monitoring --modules http-service"
        ));

        return new ProcessBuilder()
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .command(arguments)
                .start();
    }

    private JMXConnector connectToJmx() throws Exception {
        JMXServiceURL serviceURL = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:" + JMX_PORT + "/jmxrmi");

        for (int i = 0; i < WAIT_SECONDS * 2; i++) {
            try {
                return JMXConnectorFactory.connect(serviceURL, null);
            } catch (Exception e) {
                Thread.sleep(500);
            }
        }
        throw new IllegalStateException("Could not connect to JMX in " + WAIT_SECONDS + " seconds");
    }

    private void bootAmxAndWait(MBeanServerConnection mbsc) throws Exception {
        ObjectName bootAMXObjectName = new ObjectName("amx-support:type=boot-amx");

        for (int i = 0; i < WAIT_SECONDS * 2; i++) {
            if (mbsc.isRegistered(bootAMXObjectName)) {
                break;
            }
            Thread.sleep(500);
        }


        assertTrue(mbsc.isRegistered(bootAMXObjectName), "bootAMX is registered");

        mbsc.invoke(bootAMXObjectName, "bootAMX", null, null);

        // Wait for AMX runtime to be available
        for (int i = 0; i < WAIT_SECONDS * 2; i++) {
            Set<ObjectName> runtimeBeans = mbsc.queryNames(new ObjectName("amx:pp=/,type=runtime"), null);
            if (!runtimeBeans.isEmpty()) {
                return;
            }
            Thread.sleep(500);
        }
        throw new IllegalStateException("AMX runtime not available after " + WAIT_SECONDS + " seconds");
    }

    private void verifyMonitoringMBeans(MBeanServerConnection mbsc) throws Exception {
        Set<ObjectName> requestBeans = mbsc.queryNames(new ObjectName("amx:type=request-mon,*"), null);
        assertTrue(!requestBeans.isEmpty(), "Request monitoring MBean should be present");

        // Verify we can read monitoring data
        ObjectName requestBean = requestBeans.iterator().next();
        assertNotNull(mbsc.getAttribute(requestBean, "countrequests"), "Should be able to read request count");
    }

    private void cleanup(JMXConnector jmxConnector, Process process) throws InterruptedException {
        if (jmxConnector != null) try { jmxConnector.close(); } catch (Exception ignored) {}
        if (process != null && process.isAlive()) {
            process.destroyForcibly();
            process.waitFor(5, TimeUnit.SECONDS);
        }
    }

    private void cleanupFiles(File... files) {
        for (File file : files) {
            Optional.ofNullable(file).ifPresent(File::delete);
        }
    }
}
