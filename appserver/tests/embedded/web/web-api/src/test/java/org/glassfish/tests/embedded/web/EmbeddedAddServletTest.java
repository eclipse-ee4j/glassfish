/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

package org.glassfish.tests.embedded.web;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import jakarta.servlet.ServletRegistration;
import org.glassfish.embeddable.*;
import org.glassfish.embeddable.web.*;
import org.glassfish.embeddable.web.config.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests for Context#addServlet, embedded.createVirtualServer
 *
 * @author Amy Roh
 */
public class EmbeddedAddServletTest {

    static GlassFish glassfish;
    static WebContainer embedded;
    static File root;
    static String vsname = "test-server";
    static String contextRoot = "test";

    @BeforeAll
    public static void setupServer() throws GlassFishException {
        glassfish = GlassFishRuntime.bootstrap().newGlassFish();
        glassfish.start();
        embedded = glassfish.getService(WebContainer.class);
        System.out.println("================ EmbeddedAddServlet Test");
        System.out.println("Starting Web "+embedded);
        embedded.setLogLevel(Level.INFO);
        WebContainerConfig config = new WebContainerConfig();
        config.setListings(true);
        root = new File(TestConfiguration.PROJECT_DIR, "target/classes");
        config.setDocRootDir(root);
        config.setPort(8080);
        System.out.println("Added Web with base directory "+root.getAbsolutePath());
        embedded.setConfiguration(config);
    }

    @Test
    public void testEmbeddedWebAPIConfig() throws Exception {
        WebListener testListener = embedded.createWebListener("test-listener", HttpListener.class);
        testListener.setPort(9090);
        WebListener[] webListeners = new HttpListener[1];
        webListeners[0] = testListener;

        VirtualServerConfig config = new VirtualServerConfig();
        config.setHostNames("localhost");
        VirtualServer vs = (VirtualServer)
                embedded.createVirtualServer(vsname, root, webListeners);
        vs.setConfig(config);
        embedded.addVirtualServer(vs);
        boolean testvs = false;
        for (VirtualServer avs : embedded.getVirtualServers()) {
            System.out.println("virtual server "+avs.getID());
            if (avs.getID().equals(vsname)) {
                testvs=true;
            }
        }
        Assertions.assertTrue(testvs);
        Context context = (Context) embedded.createContext(root);
        ServletRegistration sr = context.addServlet("NewServlet", "org.glassfish.tests.embedded.web.NewServlet");
        sr.addMapping(new String[] {"/newservlet"});
        vs.addContext(context, contextRoot);

        URL servlet = new URL("http://localhost:9090/"+contextRoot+"/newservlet");
        URLConnection yc = servlet.openConnection();
        BufferedReader in = new BufferedReader(
                                new InputStreamReader(
                                yc.getInputStream()));

        StringBuilder sb = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null){
            sb.append(inputLine);
        }
        in.close();
        vs.removeContext(context);

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
