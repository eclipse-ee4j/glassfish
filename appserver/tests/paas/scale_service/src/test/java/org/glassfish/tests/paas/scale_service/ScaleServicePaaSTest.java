/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.tests.paas.scale_service;

import junit.framework.Assert;
import org.glassfish.embeddable.CommandResult;
import org.glassfish.embeddable.CommandRunner;
import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author bhavanishankar@dev.java.net
 */

public class ScaleServicePaaSTest {

    @Test
    public void test() throws Exception {

        // Bootstrap GlassFish DAS in embedded mode.
        GlassFish glassfish = bootstrap();

        // Deploy the PaaS app and verify it.
        runTests(glassfish);

        // Re-deploy the PaaS app and verify it.
        String testScenarios = System.getProperty("test.scenarios");
        if (testScenarios == null
                || "all".contains(testScenarios.toLowerCase())) {
            runTests(glassfish);
        }

        // 5. Stop the GlassFish DAS
        glassfish.dispose();
    }

    private void get(String urlStr, String result) throws Exception {
        URL url = new URL(urlStr);
        URLConnection yc = url.openConnection();
        System.out.println("\nURLConnection [" + yc + "] : ");
        BufferedReader in = new BufferedReader(new InputStreamReader(
                yc.getInputStream()));
        String line = null;
        boolean found = false;
        while ((line = in.readLine()) != null) {
            System.out.println(line);
            if (line.indexOf(result) != -1) {
                found = true;
            }
        }
        Assert.assertTrue(found);
        System.out.println("\n***** SUCCESS **** Found [" + result
                + "] in the response.*****\n");
    }

    private void runTests(GlassFish glassfish) throws Exception {
        // 2. Deploy the PaaS application.
        File archive = new File(System.getProperty("basedir")
                + "/target/scale_service.war"); // TODO :: use mvn apis to
        // get the archive location.
        Assert.assertTrue(archive.exists());
        Deployer deployer = null;
        String appName = null;
        try {
            deployer = glassfish.getDeployer();
            appName = deployer.deploy(archive);

            System.err.println("Deployed [" + appName + "]");
            Assert.assertNotNull(appName);

            CommandRunner commandRunner = glassfish.getCommandRunner();
            CommandResult result = commandRunner.run("list-services");
            System.out.println("\nlist-services command output [ "
                    + result.getOutput() + "]");

            // 3. Access the app to make sure PaaS app is correctly provisioned.
            String HTTP_PORT = (System.getProperty("http.port") != null) ? System
                    .getProperty("http.port") : "28080";

                    String instanceIP = getLBIPAddress(glassfish);


                    get("http://" + instanceIP +":" + HTTP_PORT
                    + "/scale_service/ScaleServicePaaSServlet",
                    "Request headers from the request:");

            // test scale up. TODO :: make sure app is accessible from the scaled instance
            result = commandRunner.run("_scale-service", "--servicename=scaleservice",
                    "--scalecount=1", "--appname=scale_service");
            System.out.println("Output of scale up service [ " + result.getOutput() + "]");
            result = commandRunner.run("list-services");
            System.out.println("Output of list-services after scaling up [ " + result.getOutput() + "]");
            String[] strings = result.getOutput().split("JavaEE");
            System.out.println("Number of Jakarta EE services " + strings.length);
            Assert.assertEquals(4, strings.length);

            // test scale down. TODO :: make sure app is no longer accessible from the scaled down instance
            result = commandRunner.run("_scale-service", "--servicename=scaleservice",
                    "--scalecount=-1", "--appname=scale_service");
            System.out.println("Output of scale down service [ " + result.getOutput() + "]");
            result = commandRunner.run("list-services");
            System.out.println("Output of list-services after scaling down [ " + result.getOutput() + "]");
            strings = result.getOutput().split("JavaEE");
            System.out.println("Number of Jakarta EE services " + strings.length);
            Assert.assertEquals(3, strings.length);

            // 4. Undeploy the PaaS application . TODO :: use cloud-undeploy??
        } finally {
            if (appName != null) {
                deployer.undeploy(appName);
                System.err.println("Undeployed [" + appName + "]");
                try {
                    boolean undeployClean = false;
                    CommandResult commandResult = glassfish.getCommandRunner()
                            .run("list-services");
                    if (commandResult.getOutput().contains("Nothing to list.")) {
                        undeployClean = true;
                    }
                    Assert.assertTrue(undeployClean);
                } catch (Exception e) {
                    System.err
                            .println("Couldn't varify whether undeploy succeeded");
                }
            }
        }

    }

    private GlassFish bootstrap() throws Exception {
        GlassFishProperties glassFishProperties = new GlassFishProperties();
        glassFishProperties.setInstanceRoot(System.getenv("S1AS_HOME")
                + "/domains/domain1");
        glassFishProperties.setConfigFileReadOnly(false);
        GlassFish glassfish = GlassFishRuntime.bootstrap().newGlassFish(
                glassFishProperties);
        PrintStream sysout = System.out;
        glassfish.start();
        System.setOut(sysout);
        return glassfish;
    }

    private String getLBIPAddress(GlassFish glassfish) {
        String lbIP = null;
        String IPAddressPattern = "IP-ADDRESS\\s*\n*(.*)\\s*\n(([01]?\\d*|2[0-4]\\d|25[0-5])\\."
                + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                + "([0-9]?\\d\\d?|2[0-4]\\d|25[0-5]))";
        try {
            CommandRunner commandRunner = glassfish.getCommandRunner();
            String result = commandRunner
                    .run("list-services", "--type", "LB",
                            "--output", "IP-ADDRESS").getOutput().toString();
            if (result.contains("Nothing to list.")) {
                result = commandRunner
                        .run("list-services", "--type", "JavaEE", "--output",
                                "IP-ADDRESS").getOutput().toString();

                Pattern p = Pattern.compile(IPAddressPattern);
                Matcher m = p.matcher(result);
                if (m.find()) {
                    lbIP = m.group(2);
                } else {
                    lbIP = "localhost";
                }
            } else {
                Pattern p = Pattern.compile(IPAddressPattern);
                Matcher m = p.matcher(result);
                if (m.find()) {
                    lbIP = m.group(2);
                } else {
                    lbIP = "localhost";
                }

            }

        } catch (Exception e) {
            System.out.println("Regex has thrown an exception "
                    + e.getMessage());
            return "localhost";
        }
        return lbIP;
    }
}
