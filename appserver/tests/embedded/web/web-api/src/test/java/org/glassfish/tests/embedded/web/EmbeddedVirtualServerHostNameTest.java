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

import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.glassfish.embeddable.web.Context;
import org.glassfish.embeddable.web.VirtualServer;
import org.glassfish.embeddable.web.WebContainer;
import org.glassfish.embeddable.web.config.VirtualServerConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test for VirtualServerConfig#setHostNames
 *
 * @author Amy Roh
 */
public class EmbeddedVirtualServerHostNameTest {

    static GlassFish glassfish;
    static WebContainer embedded;
    static String contextRoot = "host";

    @BeforeAll
    public static void setupServer() throws GlassFishException {

        GlassFishRuntime runtime = GlassFishRuntime.bootstrap();
        GlassFishProperties props = new GlassFishProperties();
        props.setPort("http-listener", 8080);
        glassfish = runtime.newGlassFish(props);
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
        assertEquals(virtualServerId,vs.getID());

        Context context = embedded.createContext(root);
        embedded.addContext(context, contextRoot);

        // curl -i -H 'Host: example.com' http://localhost:8080/
        URL servlet = new URL("http://localhost:8080/"+contextRoot+"/hello");
        URLConnection yc = servlet.openConnection();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                sb.append(inputLine);
            }
        }
        assertEquals("Hello World!", sb.toString());
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
