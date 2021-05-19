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

package org.glassfish.tests.paas.enable_disable_test;

import junit.framework.Assert;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.embeddable.*;
import org.glassfish.internal.api.Globals;
import org.junit.Test;
import org.glassfish.hk2.api.ServiceLocator;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EnableDisableTest {

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

        // 2. Deploy the PaaS application.
        File archive = new File(System.getProperty("basedir")
                + "/target/enable-disable-sample.war"); // TODO :: use
        // mvn apis to
        // get the
        // archive
        // location.
        Assert.assertTrue(archive.exists());

        Deployer deployer = null;
        String appName = null;
        try {
            deployer = glassfish.getDeployer();
            appName = deployer.deploy(archive);

            System.err.println("Deployed [" + appName + "]");
            Assert.assertNotNull(appName);

            CommandResult result = null;
            CommandRunner commandRunner = glassfish.getCommandRunner();
            {
                result = commandRunner.run("list-services", "appname="
                        + appName, "output=STATE");
                System.out.println("\nlist-services command output [ "
                        + result.getOutput() + "]");

                boolean notRunning = result.getOutput().toLowerCase()
                        .contains("notrunning");
                Assert.assertTrue(!notRunning);
                boolean stopped = result.getOutput().toLowerCase()
                        .contains("stopped");
                Assert.assertTrue(!stopped);
            }

            //2.a.Check if all services of the application are in ONLY running state.

            ServiceLocator habitat = Globals.getDefaultHabitat();
            org.glassfish.api.admin.CommandRunner commandRunner1 = habitat.getService(org.glassfish.api.admin.CommandRunner.class);
            ActionReport report = habitat.getService(ActionReport.class);
            org.glassfish.api.admin.CommandRunner.CommandInvocation invocation = commandRunner1.getCommandInvocation("list-services", report);
            ParameterMap parameterMap=new ParameterMap();
            parameterMap.add("appname",appName);
            parameterMap.add("output","state");
            parameterMap.add("scope","application");
            invocation.parameters(parameterMap).execute();

            Assert.assertFalse(report.hasFailures());
            List<Map<String,String>> listOfMap= (List<Map<String, String>>) report.getExtraProperties().get("list");
            String state=null;
            boolean servicesRunning=false;
            for(Map<String,String> map:listOfMap){
                servicesRunning=false;
                state=map.get("STATE");
                if(state.equalsIgnoreCase("running")){
                   servicesRunning=true;
                }else {
                    break;
                }
            }
            Assert.assertTrue(servicesRunning);
            System.out.println("All services in RUNNING state");

            // 3. Access the app to make sure PaaS app is correctly provisioned.
            String HTTP_PORT = (System.getProperty("http.port") != null) ? System
                    .getProperty("http.port") : "28080";

            String instanceIP = getLBIPAddress(glassfish);

            get("http://" + instanceIP + ":" + HTTP_PORT
                    + "/enable-disable-sample/EnableDisableServlet",
                    "Customer ID");


            //4.Disable application
            ParameterMap parameterMap1=new ParameterMap();
            parameterMap1.add("DEFAULT",appName);
            invocation = commandRunner1.getCommandInvocation("disable", report);
            invocation.parameters(parameterMap1).execute();

            Assert.assertFalse(report.hasFailures());
            System.out.println("Disabled application ' "+appName+" ' ");

            //5.Check if NONE of the application of the service are in 'RUNNING' state
            invocation = commandRunner1.getCommandInvocation("list-services", report);
            invocation.parameters(parameterMap).execute();

            Assert.assertFalse(report.hasFailures());
            listOfMap= (List<Map<String, String>>) report.getExtraProperties().get("list");
            for(Map<String,String> map:listOfMap){
                servicesRunning=true;
                state=map.get("STATE");
                if(state.equalsIgnoreCase("running")){
                   break;
                }else {
                    servicesRunning=false;
                }
            }
            Assert.assertFalse(servicesRunning);
            System.out.println("No service in RUNNING state");


            //6.Enable application
            invocation = commandRunner1.getCommandInvocation("enable", report);
            parameterMap1=new ParameterMap();
            parameterMap1.add("DEFAULT",appName);
            invocation.parameters(parameterMap1).execute();

            Assert.assertFalse(report.hasFailures());
            System.out.println("Enabled application ' "+appName+" ' ");

            get("http://" + instanceIP + ":" + HTTP_PORT
                    + "/enable-disable-sample/EnableDisableServlet",
                    "Customer ID");

            // 8.. Undeploy the PaaS application .
        } finally {
            if (appName != null) {
                deployer.undeploy(appName);
                System.out.println("Destroying the resources created");
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
            glassfish.dispose();
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
            if (line.contains(result)) {
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
