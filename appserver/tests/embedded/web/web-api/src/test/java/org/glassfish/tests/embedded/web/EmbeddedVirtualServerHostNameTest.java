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
import java.io.InputStreamReader;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import org.glassfish.embeddable.*;
import org.glassfish.embeddable.web.*;
import org.glassfish.embeddable.web.config.*;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test for VirtualServerConfig#setHostNames
 *
 * @author Amy Roh
 */
public class EmbeddedVirtualServerHostNameTest {

    static GlassFish glassfish;
    static WebContainer embedded;
    static String contextRoot = "host";
    static int newPort = 9090;

    @BeforeClass
    public static void setupServer() throws GlassFishException {

        GlassFishRuntime runtime = GlassFishRuntime.bootstrap();
        GlassFishProperties props = new GlassFishProperties();
        props.setPort("http-listener", 8080);
        GlassFish glassfish = runtime.newGlassFish(props);
        glassfish.start();
        embedded = glassfish.getService(WebContainer.class);

    }

    @Test
    public void test() throws Exception {

        String virtualServerId = "example";
        File root = new File(TestConfiguration.PROJECT_DIR, "target/classes");
        VirtualServer virtualServer = embedded.createVirtualServer(virtualServerId, root);

        VirtualServerConfig config = new VirtualServerConfig();
        config.setHostNames("example.com");
        virtualServer.setConfig(config);
        embedded.addVirtualServer(virtualServer);

        VirtualServer vs = embedded.getVirtualServer(virtualServerId);
        Assert.assertEquals(virtualServerId,vs.getID());

        Context context = (Context) embedded.createContext(root);
        embedded.addContext(context, contextRoot);

        // curl -i -H 'Host: example.com' http://localhost:8080/
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
        Assert.assertEquals("Hello World!", sb.toString());

    }

    @AfterClass
    public static void shutdownServer() throws GlassFishException {
        System.out.println("Stopping server " + glassfish);
        if (glassfish != null) {
            glassfish.stop();
            glassfish.dispose();
            glassfish = null;
        }
    }

}
