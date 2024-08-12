/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.tests.embedded.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.logging.Level;

import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishRuntime;
import org.glassfish.embeddable.web.Context;
import org.glassfish.embeddable.web.WebContainer;
import org.glassfish.embeddable.web.config.WebContainerConfig;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.containsString;

public class EmbeddedClassLoaderTest {

    static GlassFish glassfish;
    static String contextRoot = "test";
    static WebContainer wc;
    static File root;

    @BeforeAll
    public static void setupServer() throws GlassFishException {

        glassfish = GlassFishRuntime.bootstrap().newGlassFish();
        glassfish.start();
        root = new File(TestConfiguration.PROJECT_DIR, "target/classes");

        wc = glassfish.getService(WebContainer.class);
        wc.setLogLevel(Level.INFO);
        WebContainerConfig config = new WebContainerConfig();
        root = new File(TestConfiguration.PROJECT_DIR, "target/classes");
        config.setDocRootDir(root);
        config.setListings(true);
        config.setPort(8080);
        System.out.println("Added Web with base directory "+root.getAbsolutePath());
        wc.setConfiguration(config);
    }

    private static void loadA(ClassLoader cl) {
        String className = "TestCacaoList";
        try {
            System.out.println("---> Loading " + className + " with " + cl);
            cl.loadClass(className);
            System.out.println("---> Finish to load " + className + " with " + cl);
        } catch(Exception ex) {
            System.out.println("---> Cannot load " + className + " with " + cl + ": " + ex);
            throw new IllegalStateException();
        }
    }

    @Test
    public void test() throws Exception {
        URL[] urls = new URL[1];
        urls[0] = (new File(TestConfiguration.PROJECT_DIR, "src/main/resources/toto.jar")).toURI().toURL();
        URLClassLoader classLoader = new URLClassLoader(urls, EmbeddedClassLoaderTest.class.getClassLoader());
        loadA(classLoader);

        Thread.currentThread().setContextClassLoader(classLoader);

        File path = new File(TestConfiguration.PROJECT_DIR, "target/embedded-webapi-tests-testapp.war");

        Context context = wc.createContext(path, classLoader);

        try {
            wc.addContext(context, contextRoot);

            URL servlet = new URL("http://localhost:8080/test/testgf");
            URLConnection yc = servlet.openConnection();
            StringBuilder sb = new StringBuilder();

            try (BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    sb.append(inputLine);
                }
            }

            MatcherAssert.assertThat(sb.toString(),
                    both(containsString("Class TestCacaoList loaded successfully from listener"))
                            .and(containsString("Class TestCacaoList loaded successfully from servlet")));
        } finally {
            wc.removeContext(context);
        }
    }

    @AfterAll
    public static void shutdownServer() throws GlassFishException {
        System.out.println("Stopping server " + glassfish);
        if (glassfish != null) {
            glassfish.stop();
            glassfish.dispose();
            glassfish = null;
        }
    }

}
