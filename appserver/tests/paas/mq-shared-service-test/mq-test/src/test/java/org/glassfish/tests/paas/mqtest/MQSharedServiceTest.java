/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.tests.paas.mqtest;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.internal.api.Globals;
import org.glassfish.embeddable.CommandResult;
import org.glassfish.embeddable.CommandRunner;
import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.junit.Assert;
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

/**
 * @author Sandhya Kripalani
 */

public class MQSharedServiceTest {

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

        // 2. Deploy the PaaS-bookstore application.
        File archive = new File("/tmp/ejb30-hello-mdb-ear.ear"); // TODO :: use mvn apis to get the
        // archive location.
        org.junit.Assert.assertTrue(archive.exists());

        Deployer deployer = null;
        String appName = null;
        try {

            //Create the shared services first, as these services will be referenced by the application
            createSharedServices();

            deployer = glassfish.getDeployer();
            System.out.println("Deployer: "+deployer);
            System.out.println("archive: "+archive);
            appName = deployer.deploy(archive);

            System.err.println("Deployed [" + appName + "]");
            Assert.assertNotNull(appName);

            CommandRunner commandRunner = glassfish.getCommandRunner();
            CommandResult result = commandRunner.run("list-services");
            System.out.println("\nlist-services command output [ "
                    + result.getOutput() + "]");

            // 3. Access the app to make sure mq-shared-service-test app is correctly
            // provisioned.

            String HTTP_PORT = (System.getProperty("http.port") != null) ? System
                    .getProperty("http.port") : "28080";

            String instanceIP = getLBIPAddress(glassfish);
            System.out.println("HTTP_PORT : "+HTTP_PORT);
            get("http://" + instanceIP + ":" + HTTP_PORT
                    + "/web/mdbtest", "filterMessage=nulltestattribute=null, initParams:");

            // 4. Undeploy the MQ application .

        } finally {
            if (appName != null) {
                deployer.undeploy(appName);
                System.err.println("Undeployed [" + appName + "]");
                deleteSharedService();
                try {
                    boolean undeployClean = false;
                    CommandResult commandResult = glassfish.getCommandRunner()
                            .run("list-services");
                    System.out.println(commandResult.getOutput().toString());
                    if (commandResult.getOutput().contains("Nothing to list")) {
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

    private void createSharedServices() {

        System.out.println("################### Trying to Create Shared Services #######################");
        ServiceLocator habitat = Globals.getDefaultHabitat();
        org.glassfish.api.admin.CommandRunner commandRunner = habitat.getService(org.glassfish.api.admin.CommandRunner.class);
        ActionReport report = habitat.getService(ActionReport.class);

        org.glassfish.api.admin.CommandRunner.CommandInvocation invocation = commandRunner.getCommandInvocation("create-shared-service", report);
        ParameterMap parameterMap = new ParameterMap();
        parameterMap.add("servicetype","MQ");
        parameterMap.add("characteristics","service-type=MQ");
        parameterMap.add("configuration","mq.service.name=my-shared-mq-service");
        parameterMap.add("DEFAULT","my-shared-mq-service");
        invocation.parameters(parameterMap).execute();

        System.out.println("Created shared service 'my-shared-mq-service' :" + !report.hasFailures());
        Assert.assertFalse(report.hasFailures());

        parameterMap = new ParameterMap();
        parameterMap.add("servicetype","LB");
        parameterMap.add("characteristics", "service-type=LB");
        parameterMap.add("configuration", "http-port=50080:https-port=50081:ssl-enabled=true");
        parameterMap.add("DEFAULT","my-shared-lb-service");
        invocation.parameters(parameterMap).execute();

        System.out.println("Created shared service 'my-shared-lb-service' :" + !report.hasFailures());
        Assert.assertFalse(report.hasFailures());

        //List the services and check the status of both the services - it should be 'RUNNING'
        invocation = commandRunner.getCommandInvocation("list-services", report);
        parameterMap = new ParameterMap();
        parameterMap.add("scope", "shared");
        parameterMap.add("output", "service-name,state");
        invocation.parameters(parameterMap).execute();

        boolean sharedServiceStarted = false;
        List<Map<String, String>> list = (List<Map<String, String>>) report.getExtraProperties().get("list");
        for (Map<String, String> map : list) {
            sharedServiceStarted = false;
            String state = map.get("STATE");
            if ("RUNNING".equalsIgnoreCase(state)) {
                sharedServiceStarted = true;
            }else{
                break;
            }
        }
        Assert.assertTrue(sharedServiceStarted);//check if the shared services are started.

    }

    private void deleteSharedService() {
        ServiceLocator habitat = Globals.getDefaultHabitat();
        org.glassfish.api.admin.CommandRunner commandRunner = habitat.getService(org.glassfish.api.admin.CommandRunner.class);
        ActionReport report = habitat.getService(ActionReport.class);

        org.glassfish.api.admin.CommandRunner.CommandInvocation invocation = commandRunner.getCommandInvocation("delete-shared-service", report);
        ParameterMap parameterMap = new ParameterMap();
        parameterMap.add("DEFAULT", "my-shared-mq-service");
        invocation.parameters(parameterMap).execute();

        parameterMap = new ParameterMap();
        parameterMap.add("DEFAULT", "my-shared-lb-service");
        invocation.parameters(parameterMap).execute();

        Assert.assertFalse(report.hasFailures());

    }

}
