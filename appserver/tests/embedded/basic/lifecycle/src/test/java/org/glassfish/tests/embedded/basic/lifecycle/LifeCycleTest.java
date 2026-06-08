/*
 * Copyright (c) 2023, 2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.tests.embedded.basic.lifecycle;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.glassfish.embeddable.GlassFish.Status.DISPOSED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author bhavanishankar@dev.java.net
 */

public class LifeCycleTest {

    Logger logger = Logger.getAnonymousLogger();

    private GlassFishRuntime runtime;
    private static GlassFish instance1;
    private static GlassFish instance2;

    @TempDir
    private static File tmpDir;

    @AfterAll
    static void close() throws Exception {
        if (instance1 != null && instance1.getStatus() != DISPOSED) {
            instance1.dispose();
        }
        if (instance2 != null && instance2.getStatus() != DISPOSED) {
            instance2.dispose();
        }
    }

    @Test
    public void test() throws GlassFishException {
        runtime = GlassFishRuntime.bootstrap();

        instance1 = runtime.newGlassFish();
        logger.info("Instance1 created" + instance1);
        instance1.start();
        logger.info("Instance1 started #1");
        sleep();
        instance1.stop();
        logger.info("Instance1 stopped #1");
        instance1.start();
        logger.info("Instance1 started #2");
        sleep();
        instance1.stop();
        logger.info("Instance1 stopped #2");
        instance1.dispose();
        logger.info("Instance1 disposed");
        checkDisposed();

        GlassFishProperties props = new GlassFishProperties();
        props.setProperty("glassfish.embedded.tmpdir", tmpDir.getAbsolutePath());
        instance2 = runtime.newGlassFish(props);
        logger.info("instance2 created" + instance2);
        instance2.start();
        logger.info("Instance2 started #1");
        sleep();
        instance2.stop();
        logger.info("Instance2 stopped #1");
        instance2.start();
        logger.info("Instance2 started #2");
        sleep();
        instance2.stop();
        logger.info("Instance2 stopped #2");
        instance2.dispose();
        logger.info("Instance2 disposed");
        checkDisposed();
    }


    /**
     * Verifies that the temporary directory is deleted by the JVM shutdown hook when an embedded
     * GlassFish instance is started and stopped but never disposed (issue #25545). The scenario is
     * run in a forked JVM, because the shutdown hook only runs when the JVM exits.
     */
    @Test
    public void shutdownHookDeletesTempDirWhenNotDisposed(@TempDir File forkTmpDir) throws Exception {
        runForkedStartStopWithoutDispose(forkTmpDir);

        File[] leftovers = forkTmpDir.listFiles((dir, name) -> name.startsWith("gfembed"));
        assertNotNull(leftovers, "Could not list the temporary directory " + forkTmpDir);
        assertEquals(0, leftovers.length,
            "The shutdown hook must delete the gfembed temp directory when dispose() is not called,"
                + " but found: " + Arrays.toString(leftovers));
    }

    private void runForkedStartStopWithoutDispose(File forkTmpDir) throws Exception {
        String javaBin = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        List<String> command = new ArrayList<>();
        command.add(javaBin);
        // Forward this JVM's options (e.g. --add-opens required by embedded GlassFish).
        command.addAll(ManagementFactory.getRuntimeMXBean().getInputArguments());
        command.add("-cp");
        command.add(buildClasspath());
        command.add(StartStopNoDisposeRunner.class.getName());
        command.add(forkTmpDir.getAbsolutePath());

        Process process = new ProcessBuilder(command).inheritIO().start();
        if (!process.waitFor(5, TimeUnit.MINUTES)) {
            process.destroyForcibly();
            fail("The forked embedded GlassFish process did not finish in time.");
        }
        assertEquals(0, process.exitValue(), "The forked embedded GlassFish process failed.");
    }

    /**
     * Builds the classpath for the forked JVM. Surefire often runs tests in an isolated
     * {@link URLClassLoader}, so {@code java.class.path} alone does not contain the test
     * dependencies (including the embedded GlassFish uber jar). Collect the URLs from the
     * classloader hierarchy and fall back to {@code java.class.path} if none are found.
     */
    private static String buildClasspath() throws Exception {
        StringBuilder classpath = new StringBuilder();
        for (ClassLoader cl = LifeCycleTest.class.getClassLoader(); cl != null; cl = cl.getParent()) {
            if (cl instanceof URLClassLoader urlClassLoader) {
                for (URL url : urlClassLoader.getURLs()) {
                    if (classpath.length() > 0) {
                        classpath.append(File.pathSeparatorChar);
                    }
                    classpath.append(new File(url.toURI()).getAbsolutePath());
                }
            }
        }
        return classpath.length() > 0 ? classpath.toString() : System.getProperty("java.class.path");
    }


    private void sleep() {
        try {
            Thread.sleep(1000);
        } catch (Exception ex) {
        }
    }
    // throws exception if the temp dir is not cleaned out.


    private void checkDisposed() {
        String instanceRoot = System.getProperty("com.sun.aas.instanceRoot");
        logger.info("Checking whether " + instanceRoot + " is disposed or not");
        if (new File(instanceRoot).exists()) {
            throw new RuntimeException("Directory " + instanceRoot + " is not cleaned up after glassfish.dispose()");
        }
    }
}
