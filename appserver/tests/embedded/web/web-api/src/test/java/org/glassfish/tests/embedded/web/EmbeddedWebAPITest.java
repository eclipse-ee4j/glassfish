/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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
import java.util.List;
import java.util.logging.Level;

import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishRuntime;
import org.glassfish.embeddable.web.HttpListener;
import org.glassfish.embeddable.web.VirtualServer;
import org.glassfish.embeddable.web.WebContainer;
import org.glassfish.embeddable.web.WebListener;
import org.glassfish.embeddable.web.config.VirtualServerConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests creating a port using WebContainer#createWeblistener & WebListener#setPort.
 * Checks if network listener is correctly added and deployment suceeds on the port specified.
 *
 * @author Amy Roh
 */
public class EmbeddedWebAPITest {

    static GlassFish glassfish;
    static WebContainer embedded;
    static String contextRoot = "test";

    @BeforeAll
    public static void setupServer() throws GlassFishException {
        glassfish = GlassFishRuntime.bootstrap().newGlassFish();
        glassfish.start();
        embedded = glassfish.getService(WebContainer.class);
        System.out.println("================ Test Embedded Web API");
        System.out.println("Starting Web "+embedded);
        embedded.setLogLevel(Level.INFO);
    }

    @Test
    public void testEmbeddedWebAPI() throws Exception {

        HttpListener httpListener = new HttpListener();
        httpListener.setPort(8080);
        httpListener.setId("embedded-listener-1");
        embedded.addWebListener(httpListener);

        List<WebListener> listenerList = new ArrayList(embedded.getWebListeners());
        Assertions.assertTrue(listenerList.size()==1);
        for (WebListener listener : embedded.getWebListeners())
            System.out.println("Web listener "+listener.getId()+" "+listener.getPort());

        WebListener testListener = embedded.createWebListener("test-listener", HttpListener.class);
        testListener.setPort(9090);
        embedded.addWebListener(testListener);

        listenerList = new ArrayList(embedded.getWebListeners());
        System.out.println("Network listener size after creation " + listenerList.size());
        Assertions.assertTrue(listenerList.size()==2);
        for (WebListener listener : embedded.getWebListeners())
            System.out.println("Web listener "+listener.getId()+" "+listener.getPort());

        File f = new File(TestConfiguration.PROJECT_DIR, "target/classes");
        String virtualServerId = "embedded-server";
        VirtualServer vs = (VirtualServer)
                embedded.createVirtualServer(virtualServerId, f);
        VirtualServerConfig config = new VirtualServerConfig();
        config.setHostNames("localhost");
        vs.setConfig(config);
        embedded.addVirtualServer(vs);

        vs = embedded.getVirtualServer(virtualServerId);
        Assertions.assertEquals(virtualServerId,vs.getID());

        //Context context = (Context) embedded.createContext(root, null);
        //defaultVirtualServer.addContext(context, "");

        Deployer deployer = glassfish.getDeployer();

        URL source = WebHello.class.getClassLoader().getResource(
                "org/glassfish/tests/embedded/web/WebHello.class");
        String p = source.getPath().substring(0, source.getPath().length() -
                "org/glassfish/tests/embedded/web/WebHello.class".length());
        File path = new File(p).getParentFile().getParentFile();

        String name = null;

        if (path.getName().lastIndexOf('.') != -1) {
            name = path.getName().substring(0, path.getName().lastIndexOf('.'));
        } else {
            name = path.getName();
        }

        System.out.println("Deploying " + path + ", name = " + name);

        String appName = deployer.deploy(path.toURI(), "--contextroot", contextRoot, "--name=" + name);

        System.out.println("Deployed " + appName);

        Assertions.assertTrue(appName != null);

        URL servlet = new URL("http://localhost:8080/"+contextRoot+"/hello");
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

        embedded.removeWebListener(testListener);

        listenerList = new ArrayList(embedded.getWebListeners());
        System.out.println("Network listener size after creation " + listenerList.size());
        Assertions.assertTrue(listenerList.size()==1);
        for (WebListener listener : embedded.getWebListeners())
            System.out.println("Web listener "+listener.getId()+" "+listener.getPort());

        Thread.sleep(1000);

        if (appName!=null)
            deployer.undeploy(appName);
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
