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
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.logging.Level;

import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishRuntime;
import org.glassfish.embeddable.web.Context;
import org.glassfish.embeddable.web.HttpListener;
import org.glassfish.embeddable.web.VirtualServer;
import org.glassfish.embeddable.web.WebContainer;
import org.glassfish.embeddable.web.WebListener;
import org.glassfish.embeddable.web.config.WebContainerConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests WebContainer
 *
 * @author Amy Roh
 */
public class EmbeddedWebTest {

    static GlassFish glassfish;
    static WebContainer embedded;
    static int newPort = 9090;
    static String contextRoot = "test";
    static File root;
    static WebContainerConfig config;

    @BeforeAll
    public static void setupServer() throws Exception {

        glassfish = GlassFishRuntime.bootstrap().newGlassFish();
        glassfish.start();
        embedded = glassfish.getService(WebContainer.class);
        System.out.println("================ EmbeddedWeb Test");
        System.out.println("Starting Web "+embedded);
        root = new File(TestConfiguration.PROJECT_DIR, "target/classes");
        configure();

    }

    private static void configure() throws Exception {

        config = new WebContainerConfig();
        embedded.setLogLevel(Level.INFO);
        config.setDocRootDir(root);
        System.out.println("Added Web with base directory "+root.getAbsolutePath());
        config.setListenerName("embedded-listener");
        config.setPort(8080);
        File defaultWebXml = new File(root+"/org/glassfish/tests/webapi/my-default-web.xml");
        config.setDefaultWebXml(defaultWebXml.toURL());
        System.out.println("Using default-web.xml "+defaultWebXml.getAbsolutePath());
        config.setVirtualServerId("server");
        embedded.setConfiguration(config);

    }

    @Test
    public void test() throws Exception {

        WebListener testListener = embedded.createWebListener("test-listener", HttpListener.class);
        testListener.setPort(newPort);
        WebListener[] webListeners = new HttpListener[1];
        webListeners[0] = testListener;

        String virtualServerId = "embedded-server";
        VirtualServer virtualServer = embedded.createVirtualServer(virtualServerId, root, webListeners);
        embedded.addVirtualServer(virtualServer);

        config.setVirtualServerId(virtualServerId);
        config.setHostNames("localhost");
        embedded.setConfiguration(config);

        ArrayList<WebListener> listenerList = new ArrayList(embedded.getWebListeners());
        System.out.println("Network listener size after creation " + listenerList.size());
        Assertions.assertTrue(listenerList.size()==2);
        for (WebListener listener : embedded.getWebListeners()) {
            System.out.println("Web listener "+listener.getId()+" "+listener.getPort());
        }

        VirtualServer vs = embedded.getVirtualServer(virtualServerId);
        assertEquals(virtualServerId,vs.getID());

        Context context = embedded.createContext(root);
        //embedded.addContext(context, contextRoot);
        virtualServer.addContext(context, contextRoot);

        URL servlet = new URL("http://localhost:"+newPort+"/"+contextRoot+"/hello");
        URLConnection yc = servlet.openConnection();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                sb.append(inputLine);
            }
        }
        assertEquals("Hello World!", sb.toString());

        System.out.println("Removing web listener "+testListener.getId());
        embedded.removeWebListener(testListener);

        listenerList = new ArrayList(embedded.getWebListeners());
        System.out.println("Network listener size after deletion " + listenerList.size());
        Assertions.assertTrue(listenerList.size()==1);
        for (WebListener listener : embedded.getWebListeners()) {
            System.out.println("Web listener "+listener.getId()+" "+listener.getPort());
        }

        virtualServer.removeContext(context);

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
