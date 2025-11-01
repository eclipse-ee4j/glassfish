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

package org.glassfish.main.test.perf.embedded;

import jakarta.ws.rs.client.WebTarget;

import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;

import org.glassfish.main.test.perf.benchmark.Environment;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import static java.lang.System.Logger.Level.INFO;
import static org.glassfish.main.test.perf.embedded.EmbeddedGlassFishContainer.WAR_FILE;
import static org.testcontainers.utility.MountableFile.forHostPath;

/**
 * Environment of Eclipse GlassFish, Derby and Postgress SQL databases.
 */
public class DockerTestEnvironmentWithEmbedded extends Environment {

    private static final Logger LOG = System.getLogger(DockerTestEnvironmentWithEmbedded.class.getName());

    private final EmbeddedGlassFishContainer app;

    /**
     * Creates network, databases and application server, but doesn't start them.
     */
    public DockerTestEnvironmentWithEmbedded() {
        app = new EmbeddedGlassFishContainer(getNetwork(), "admin", "A", getDatabase());
        Thread hook = new Thread(this::stop);
        Runtime.getRuntime().addShutdownHook(hook);
    }


    @Override
    public WebTarget start(String appName, WebArchive war) {
        super.start();
        undeploy(appName);
        return deploy(appName, war);
    }

    @Override
    public WebTarget deploy(String appName, WebArchive war) {
        app.withCopyFileToContainer(forHostPath(toFile("webapp", war).getPath()), WAR_FILE);
        app.start();
        return app.getRestClient("");
    }

    @Override
    public void undeploy(String appname) {
        if (app.isRunning()) {
            app.stop();
        }
    }

    @Override
    public void stop() {
        if (app.isRunning()) {
            closeSilently(app);
        }
        super.stop();
    }


    private static File toFile(final String appName, final WebArchive war) {
        LOG.log(INFO, () -> war.toString(true));
        File warFile;
        try {
            warFile = File.createTempFile(appName, ".war");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        war.as(ZipExporter.class).exportTo(warFile, true);
        return warFile;
    }
}
