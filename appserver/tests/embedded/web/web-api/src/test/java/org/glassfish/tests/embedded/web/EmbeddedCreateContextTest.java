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
import org.glassfish.embeddable.*;
import org.glassfish.embeddable.web.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests WebContainer#createContext
 *
 * @author Amy Roh
 */
public class EmbeddedCreateContextTest {

    static GlassFish glassfish;
    static WebContainer embedded;
    static String contextRoot = "test";

    @BeforeClass
    public static void setupServer() throws GlassFishException {
        glassfish = GlassFishRuntime.bootstrap().newGlassFish();
        glassfish.start();
        embedded = glassfish.getService(WebContainer.class);
        System.out.println("================ EmbeddedCreateContext Test");
        System.out.println("Starting Web "+embedded);
        embedded.setLogLevel(Level.INFO);
    }

    @Test
    public void test() throws Exception {

        HttpListener listener = new HttpListener();
        listener.setPort(8080);
        listener.setId("embedded-listener-1");
        embedded.addWebListener(listener);

        File docRoot = new File(TestConfiguration.PROJECT_DIR, "target/classes");
        Context context = (Context) embedded.createContext(docRoot, contextRoot, null);

        URL servlet = new URL("http://localhost:8080/"+contextRoot+"/hello");
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
