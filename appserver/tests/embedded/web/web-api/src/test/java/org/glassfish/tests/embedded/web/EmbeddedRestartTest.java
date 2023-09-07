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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests GlassFish restart
 *
 * @author Amy Roh
 */
public class EmbeddedRestartTest {

    static GlassFish glassfish;
    static String contextRoot = "test";

    @BeforeAll
    public static void setupServer() throws GlassFishException {
        GlassFishProperties gp = new GlassFishProperties();
        gp.setPort("http-listener", 8080);
        glassfish = GlassFishRuntime.bootstrap().newGlassFish(gp);
        glassfish.start();
        System.out.println("================ Embedded Restart Test");
    }

    @Test
    public void testEmbeddedWebAPI() throws Exception {

        // Restart is not working. Uncomment this to see the issue.
        //glassfish.stop();
        //glassfish.start();

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

        System.out.println("Deploying " + path + ", contextroot = " + contextRoot);

        String appName = deployer.deploy(path.toURI(), "--contextroot", contextRoot);

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

        System.out.println("Undeploying "+appName);
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
