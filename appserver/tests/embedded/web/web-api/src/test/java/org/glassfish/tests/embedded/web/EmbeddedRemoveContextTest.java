/*
 * Copyright (c) 2023, 2025 Contributors to the Eclipse Foundation.
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
import java.net.URLConnection;
import java.util.logging.Level;

import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishRuntime;
import org.glassfish.embeddable.web.Context;
import org.glassfish.embeddable.web.VirtualServer;
import org.glassfish.embeddable.web.WebContainer;
import org.glassfish.embeddable.web.config.WebContainerConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests WebContainer#removeContext
 *
 * @author Amy Roh
 */
public class EmbeddedRemoveContextTest {

    static GlassFish glassfish;
    static WebContainer embedded;
    static File root;
    static String contextRoot = "test";

    @BeforeAll
    public static void setupServer() throws GlassFishException {
        glassfish = GlassFishRuntime.bootstrap().newGlassFish();
        glassfish.start();
        embedded = glassfish.getService(WebContainer.class);
        System.out.println("================ EmbeddedRemoveContext Test");
        System.out.println("Starting Web "+embedded);
        embedded.setLogLevel(Level.INFO);
        WebContainerConfig config = new WebContainerConfig();
        root = new File(TestConfiguration.PROJECT_DIR, "target/classes");
        config.setDocRootDir(root);
        config.setListings(true);
        config.setPort(8080);
        System.out.println("Added Web with base directory "+root.getAbsolutePath());
        embedded.setConfiguration(config);
    }

    @Test
    public void test() throws Exception {

        Context context = embedded.createContext(root);
        embedded.addContext(context, contextRoot);

        VirtualServer vs = embedded.getVirtualServer("server");
        assertEquals("server", vs.getID());
        assertEquals("/"+contextRoot, vs.getContext(contextRoot).getPath());
        boolean containsContext = false;
        for (Context ctx : vs.getContexts()) {
            System.out.println("Context found "+ctx.getPath());
            if (ctx.getPath().endsWith(contextRoot)) {
                containsContext = true;
            }
        }
        assertTrue(containsContext);

        URL servlet = new URL("http://localhost:8080/"+contextRoot+"/hello");
        URLConnection yc = servlet.openConnection();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                sb.append(inputLine);
            }
        }

        embedded.removeContext(context);

        assertNull(vs.getContext(contextRoot));

        containsContext = false;
        for (Context ctx : vs.getContexts()) {
            System.out.println("Context found "+ctx.getPath());
            if (ctx.getPath().endsWith(contextRoot)) {
                containsContext = true;
            }
        }
        assertTrue(!containsContext);

        embedded.addContext(context, contextRoot);

        assertEquals("/"+contextRoot, vs.getContext(contextRoot).getPath());
        for (Context ctx : vs.getContexts()) {
            System.out.println("Context found "+ctx.getPath());
            if (ctx.getPath().endsWith(contextRoot)) {
                containsContext = true;
            }
        }
        assertTrue(containsContext);
        yc = servlet.openConnection();
        sb = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                sb.append(inputLine);
            }
        }

        embedded.removeContext(context);
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
