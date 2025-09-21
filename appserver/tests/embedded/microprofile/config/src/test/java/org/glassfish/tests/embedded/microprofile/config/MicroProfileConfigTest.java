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

package org.glassfish.tests.embedded.microprofile.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.glassfish.embeddable.archive.ScatteredArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test MicroProfile Config functionality in embedded GlassFish
 */
public class MicroProfileConfigTest {

    private static final String PROJECT_DIR = System.getProperty("basedir", System.getProperty("user.dir"));
    private static final int HTTP_PORT = 8080;
    private static final String APP_NAME = "config";

    static GlassFish glassfish;
    static String appName;

    @BeforeAll
    static void setupServer() throws Exception {
        GlassFishProperties props = new GlassFishProperties();
        props.setPort("http-listener", HTTP_PORT);
        glassfish = GlassFishRuntime.bootstrap().newGlassFish(props);
        glassfish.start();

        File classesDir = new File(PROJECT_DIR, "target/classes");

        ScatteredArchive sa = new ScatteredArchive(APP_NAME, ScatteredArchive.Type.WAR);
        sa.addClassPath(classesDir);
        URI warURI = sa.toURI();

        Deployer deployer = glassfish.getDeployer();
        appName = deployer.deploy(warURI);
    }

    @Test
    void testConfigValueInjection() throws Exception {
        URL servlet = new URL("http://localhost:" + HTTP_PORT + "/" + APP_NAME + "/config");
        URLConnection connection = servlet.openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String response = reader.readLine();
        reader.close();

        assertEquals("Hello MicroProfile,Injected", response);
    }

    @AfterAll
    static void shutdownServer() throws Exception {
        if (appName != null) {
            glassfish.getDeployer().undeploy(appName);
        }
        if (glassfish != null) {
            glassfish.dispose();
        }
    }
}
