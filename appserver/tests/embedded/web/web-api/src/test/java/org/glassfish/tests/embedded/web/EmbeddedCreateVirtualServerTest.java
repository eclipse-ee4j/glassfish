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
import java.util.ArrayList;
import java.util.List;
import org.glassfish.embeddable.*;
import org.glassfish.embeddable.web.*;
import org.glassfish.embeddable.web.config.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests WebContainer#createVirtualServerTest
 *
 * @author Amy Roh
 */
public class EmbeddedCreateVirtualServerTest {

    static GlassFish glassfish;
    static WebContainer embedded;
    static int newPort = 9090;
    static String contextRoot = "test";

    @BeforeAll
    public static void setupServer() throws GlassFishException {
        glassfish = GlassFishRuntime.bootstrap().newGlassFish();
        glassfish.start();
        embedded = glassfish.getService(WebContainer.class);
        System.out.println("================ EmbeddedCreateVirtualServer Test");
        System.out.println("Starting Web "+embedded);
        embedded.setLogLevel(Level.INFO);
    }

    @Test
    public void test() throws Exception {

        HttpListener httpListener = new HttpListener();
        httpListener.setPort(8080);
        httpListener.setId("embedded-listener-1");
        embedded.addWebListener(httpListener);

        List<WebListener> listenerList = new ArrayList(embedded.getWebListeners());
        Assertions.assertTrue(listenerList.size()==1);
        for (WebListener listener : embedded.getWebListeners())
            System.out.println("Web listener "+listener.getId()+" "+listener.getPort());

        WebListener testListener = embedded.createWebListener("test-listener", HttpListener.class);
        testListener.setPort(newPort);
        WebListener[] webListeners = new HttpListener[1];
        webListeners[0] = testListener;

        File f = new File(TestConfiguration.PROJECT_DIR, "target/classes");
        String virtualServerId = "embedded-server";
        VirtualServer virtualServer = (VirtualServer)
                embedded.createVirtualServer(virtualServerId, f, webListeners);
        VirtualServerConfig config = new VirtualServerConfig();
        config.setHostNames("localhost");
        virtualServer.setConfig(config);
        embedded.addVirtualServer(virtualServer);

        listenerList = new ArrayList(embedded.getWebListeners());
        System.out.println("Network listener size after creation " + listenerList.size());
        Assertions.assertTrue(listenerList.size()==2);
        for (WebListener listener : embedded.getWebListeners())
            System.out.println("Web listener "+listener.getId()+" "+listener.getPort());

        VirtualServer vs = embedded.getVirtualServer(virtualServerId);
        Assertions.assertEquals(virtualServerId,vs.getID());

        File docRoot = new File(TestConfiguration.PROJECT_DIR, "target/classes");
        Context context = (Context) embedded.createContext(docRoot);
        vs.addContext(context, contextRoot);

        URL servlet = new URL("http://localhost:"+newPort+"/"+contextRoot+"/hello");
        URLConnection yc = servlet.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null){
            sb.append(inputLine);
        }
        in.close();
        System.out.println(inputLine);
        Assertions.assertEquals("Hello World!", sb.toString());

        vs.removeContext(context);
        System.out.println("Removing web listener "+testListener.getId());
        embedded.removeWebListener(testListener);

        listenerList = new ArrayList(embedded.getWebListeners());
        System.out.println("Network listener size after deletion " + listenerList.size());
        Assertions.assertTrue(listenerList.size()==1);
        for (WebListener listener : embedded.getWebListeners())
            System.out.println("Web listener "+listener.getId()+" "+listener.getPort());
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
