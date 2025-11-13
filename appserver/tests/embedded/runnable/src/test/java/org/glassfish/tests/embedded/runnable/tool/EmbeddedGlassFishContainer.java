/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.tests.embedded.runnable.tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.System.Logger;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

/**
 * Wrapper for Embedded GlassFish process.
 *
 * @author David Matejcek
 */
public final class EmbeddedGlassFishContainer {
    private static final Logger LOG = System.getLogger(EmbeddedGlassFishContainer.class.getName());

    private final String gfEmbeddedJarName;
    private final Path warFile;
    private Process process;


    /**
     * Creates the container. Doesn't start anything yet.
     *
     * @param gfEmbeddedJarName
     * @param warFile
     */
    public EmbeddedGlassFishContainer(String gfEmbeddedJarName, Path warFile) {
        this.gfEmbeddedJarName = gfEmbeddedJarName;
        this.warFile = warFile;
    }


    /**
     * Starts the server.
     */
    public void start() {
        try {
            process = EmbeddedGlassFishStarter.start(gfEmbeddedJarName, true,
                List.of("-Dcom.sun.management.jmxremote", "-javaagent:flashlight-agent.jar"),
                "enable-monitoring --modules http-service", warFile.toString());
            LOG.log(INFO, "Started: " + gfEmbeddedJarName);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start the " + gfEmbeddedJarName, e);
        }
    }


    /**
     * @return true if the process is running.
     */
    public boolean isAlive() {
        return process != null && process.isAlive();
    }


    /**
     * Stops the server.
     */
    public void stop() {
        LOG.log(INFO, "Stopping the Embedded GlassFish process...");
        process.destroy();
        try {
            process.waitFor(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOG.log(ERROR, "Waiting for the process death interrupted.", e);
            Thread.currentThread().interrupt();
        }
    }


    /**
     * Starts thread parsing the server STDERR output and logs it with a DEBUG level.
     * The thread is a daemon thread; it can also stop when the server process stops.
     * <p>
     * It is critical for Windows OS which would block server's actions because nobody
     * else would read the STDERR output otherwise and buffers would be full soon.
     */
    public void startReadingStdErr() {
        Thread stderrProcessor = new Thread(
            () -> getErrorReader().lines().forEach(line -> LOG.log(DEBUG, line)), "GF-StdErr");
        stderrProcessor.setDaemon(true);
        stderrProcessor.start();
    }


    /**
     * @return the STDERR output of the server.
     */
    public BufferedReader getErrorReader() {
        while (process == null) {
            Thread.onSpinWait();
        }
        return process.errorReader();
    }
}
