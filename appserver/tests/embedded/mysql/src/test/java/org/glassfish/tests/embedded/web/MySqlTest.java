/*
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

import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.junit.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author bhavanishankar@java.net
 */

public class MySqlTest {

    @Test
    public void test() throws Exception {
        GlassFishProperties glassFishProperties = new GlassFishProperties();
        glassFishProperties.setPort("http-listener", 8080);

        GlassFish glassFish = GlassFishRuntime.bootstrap().
                newGlassFish(glassFishProperties);

        glassFish.start();

        Deployer deployer = glassFish.getDeployer();
        String appName = deployer.deploy(new File("target/mysqltest.war"));
        System.out.println("Deployed [" + appName + "]");


        // Access the app
        get("http://localhost:8080/mysqltest/mysqlTestServlet", "connection = ");

        glassFish.dispose();

    }

    private static void get(String url, String result) throws Exception {
        try {
            URL servlet = new URL(url);
            HttpURLConnection uc = (HttpURLConnection) servlet.openConnection();
            System.out.println("\nURLConnection = " + uc + " : ");
            if (uc.getResponseCode() != 200) {
                throw new Exception("Servlet did not return 200 OK response code");
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    uc.getInputStream()));
            String line = null;
            boolean found = false;
            int index;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
                index = line.indexOf(result);
                if (index != -1) {
                    found = true;
                }
            }
            Assert.assertTrue(found);
            System.out.println("\n***** SUCCESS **** Found [" + result + "] in the response.*****\n");
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
