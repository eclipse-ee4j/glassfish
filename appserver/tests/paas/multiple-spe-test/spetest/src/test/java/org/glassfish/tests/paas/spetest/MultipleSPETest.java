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

package org.glassfish.tests.paas.spetest;

import junit.framework.Assert;
import org.glassfish.embeddable.CommandResult;
import org.glassfish.embeddable.CommandRunner;
import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.internal.api.Globals;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.paas.orchestrator.PaaSDeploymentException;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.Exception;
import java.lang.System;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Sandhya Kripalani
 */

public class MultipleSPETest {

    @Test
    public void test() throws Exception {

        // 1. Bootstrap GlassFish DAS in embedded mode.
        GlassFishProperties glassFishProperties = new GlassFishProperties();
        glassFishProperties.setInstanceRoot(System.getenv("S1AS_HOME")
                + "/domains/domain1");
        glassFishProperties.setConfigFileReadOnly(false);
        GlassFish glassfish = GlassFishRuntime.bootstrap().newGlassFish(
                glassFishProperties);
        PrintStream sysout = System.out;
        glassfish.start();
        System.setOut(sysout);

        // 2. Deploy the PaaS-bookstore application. Deployment should fail
        File archive = new File(System.getProperty("basedir")
                + "/target/basic-spe-test.war"); // TODO :: use mvn apis to get the
        // archive location.
        Assert.assertTrue(archive.exists());

        Deployer deployer = null;
        String appName = null;
        try {
            deployer = glassfish.getDeployer();
            appName = deployer.deploy(archive);

            System.err.println("Deployed [" + appName + "]");
            Assert.assertNull(appName);
        } catch (Exception e) {
            System.out.println("$$$$$$$$$$$$$$$$Exception$$$$$$");
        } finally {

            //3. Register one of the plugins as the default S.P.E
            ServiceLocator habitat = Globals.getDefaultHabitat();
            org.glassfish.api.admin.CommandRunner commandRunner = habitat.getService(org.glassfish.api.admin.CommandRunner.class);
            ActionReport report = habitat.getService(ActionReport.class);

            org.glassfish.api.admin.CommandRunner.CommandInvocation invocation = commandRunner.getCommandInvocation("register-service-provisioning-engine", report);
            ParameterMap parameterMap = new ParameterMap();

            parameterMap.add("type", "Database");
            parameterMap.add("defaultservice", "true");
            parameterMap.add("DEFAULT", "org.glassfish.paas.mydbplugin.MyDBPlugin");

            invocation.parameters(parameterMap).execute();

            Assert.assertFalse(report.hasFailures());
            System.out.println("Registered a default SPE :" + !report.hasFailures());

            //4. Deploy the application. Deployment should succeed.
            appName = deployer.deploy(archive);

            System.err.println("Deployed [" + appName + "]");
            Assert.assertNotNull(appName);

            // 5. Access the app to make sure PaaS-basic-shared-service-test app is correctly
            // provisioned.

            String HTTP_PORT = (System.getProperty("http.port") != null) ? System
                    .getProperty("http.port") : "28080";

            String instanceIP = getLBIPAddress(glassfish);

            get("http://" + instanceIP + ":" + HTTP_PORT
                    + "/basic-spe-test/list", "Here is a list of animals in the zoo.");

            //Retrieve the  port number used by the connection pool
            invocation=commandRunner.getCommandInvocation("get", report);
            parameterMap = new ParameterMap();

            parameterMap.add("DEFAULT", "server.resources.jdbc-connection-pool.jdbc/__multiple_spe_paas_sample.property.PortNumber");
            invocation.parameters(parameterMap).execute();

            Assert.assertFalse(report.hasFailures());

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
                    System.err.println("Couldn't varify whether undeploy succeeded");
                }
            }
            commandRunner = habitat.getService(org.glassfish.api.admin.CommandRunner.class);
            invocation = commandRunner.getCommandInvocation("unregister-service-provisioning-engine", report);
            parameterMap = new ParameterMap();
            parameterMap.add("DEFAULT", "org.glassfish.paas.mydbplugin.MyDBPlugin");

            invocation.parameters(parameterMap).execute();

            Assert.assertFalse(report.hasFailures());
            System.out.println("Unregistered the default SPE :" + !report.hasFailures());
        }
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
