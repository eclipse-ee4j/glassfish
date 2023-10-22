/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package org.glassfish.tests.paas.external_and_shared_service;

import com.sun.enterprise.universal.process.ProcessManager;
import com.sun.enterprise.universal.process.ProcessManagerException;
import com.sun.enterprise.util.OS;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.embeddable.*;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.api.ServerContext;
import org.junit.Assert;
import org.junit.Test;
import org.glassfish.hk2.api.ServiceLocator;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Sandhya Kripalani
 */

public class SharedAndExternalServiceTest {
    
    private static final int DATABASE_TIMEOUT = 60_000;

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
        File archive = new File(System.getProperty("basedir")
                + "/target/external-and-shared-service-test.war"); // TODO :: use mvn apis to get the
        // archive location.
        org.junit.Assert.assertTrue(archive.exists());

        //Obtaining the IP address of the DAS
        String ip_address="127.0.0.1";
        try{
        Enumeration netint_enum= NetworkInterface.getNetworkInterfaces();
        for (Iterator it = Collections.list(netint_enum).iterator(); it.hasNext();) {
                NetworkInterface netint = (NetworkInterface) it.next();
            if(netint.getName().equals("virbr0")){
                Enumeration inetAddresses=netint.getInetAddresses();
                if(inetAddresses.hasMoreElements())
                {
                    InetAddress inetAddress=(InetAddress)inetAddresses.nextElement();
                    ip_address=inetAddress.toString();
                    ip_address=ip_address.substring(1,ip_address.length());
                    break;
                }

            }
            }
        }catch(SocketException socketException){
            socketException.printStackTrace();
        }

        Deployer deployer = null;
        String appName = null;
        try {
            {
                //start-database
                ServiceLocator habitat = Globals.getDefaultHabitat();
                ServerContext serverContext = habitat.getService(ServerContext.class);
                String[] startdbArgs = {serverContext.getInstallRoot().getAbsolutePath() +
                        File.separator + "bin" + File.separator + "asadmin" + (OS.isWindows() ? ".bat" : ""), "start-database",
                        "--dbhome" , serverContext.getInstallRoot().getAbsolutePath() + File.separator + "databases","--dbhost",ip_address};
                ProcessManager startDatabase = new ProcessManager(startdbArgs);
                startDatabase.setTimeoutMsec(DATABASE_TIMEOUT);

                try {
                    startDatabase.execute();
                } catch (ProcessManagerException e) {
                    e.printStackTrace();
                }
            }


            //Create the shared & external services first, as these services will be referenced by the application
            createSharedAndExternalServices(ip_address);

            deployer = glassfish.getDeployer();
            appName = deployer.deploy(archive);

            System.err.println("Deployed [" + appName + "]");
            Assert.assertNotNull(appName);

            CommandRunner commandRunner = glassfish.getCommandRunner();
            CommandResult result = commandRunner.run("list-services");
            System.out.println("\nlist-services command output [ "
                    + result.getOutput() + "]");

            // 3. Access the app to make sure PaaS-external-and-shared-service-test app is correctly
            // provisioned.

            String HTTP_PORT = (System.getProperty("http.port") != null) ? System
                    .getProperty("http.port") : "28080";

            String instanceIP = getLBIPAddress(glassfish);

            get("http://" + instanceIP + ":" + HTTP_PORT
                    + "/external-and-shared-service-test/list", "Here is a list of animals in the zoo.");

            testSharedAndExternalService();

            // 4. Access the app to make sure PaaS-external-and-shared-service-test app is correctly
            // provisioned after running Shared-Services test

            get("http://" + instanceIP + ":" + HTTP_PORT
                    + "/external-and-shared-service-test/list", "Here is a list of animals in the zoo.");

            // 5. Undeploy the Zoo catalogue application .

        } finally {
            if (appName != null) {
                deployer.undeploy(appName);
                System.err.println("Undeployed [" + appName + "]");
                deleteSharedAndExternalService();

                {
                    //stop-database
                    ServiceLocator habitat = Globals.getDefaultHabitat();
                    ServerContext serverContext = habitat.getService(ServerContext.class);
                    String[] stopDbArgs = {serverContext.getInstallRoot().getAbsolutePath() +
                            File.separator + "bin" + File.separator + "asadmin" + (OS.isWindows() ? ".bat" : ""), "stop-database","--dbhost",ip_address};
                    ProcessManager stopDatabase = new ProcessManager(stopDbArgs);
                    stopDatabase.setTimeoutMsec(DATABASE_TIMEOUT);

                    try {
                        stopDatabase.execute();
                    } catch (ProcessManagerException e) {
                        e.printStackTrace();
                    }
                }

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

    private void createSharedAndExternalServices(String ipAddress_DAS) {

        System.out.println("################### Trying to Create Shared Service #######################");
        ServiceLocator habitat = Globals.getDefaultHabitat();
        org.glassfish.api.admin.CommandRunner commandRunner = habitat.getService(org.glassfish.api.admin.CommandRunner.class);
        ActionReport report = habitat.getService(ActionReport.class);

        //Create external service of type Database
        // asadmin create-external-service --servicetype=Database --configuration ip-address=127.0.0.1:databasename=sun-appserv-samples:port=1527:user=APP:password=APP:host=127.0.0.1:classname=org.apache.derby.jdbc.ClientXADataSource:resourcetype=javax.sql.XADataSource my-external-db-service
        org.glassfish.api.admin.CommandRunner.CommandInvocation invocation = commandRunner.getCommandInvocation("create-external-service", report);
        ParameterMap parameterMap = new ParameterMap();
        parameterMap.add("servicetype", "Database");
        parameterMap.add("configuration", "ip-address="+ipAddress_DAS+":databasename=sun-appserv-samples:connectionAttributes=;'create=true':port=1527:user=APP:password=APP:host="+ipAddress_DAS+":classname=org.apache.derby.jdbc.ClientXADataSource:resourcetype=javax.sql.XADataSource");
        //parameterMap.add("configuration", "ip-address=127.0.0.1:databasename=${com.sun.aas.installRoot}/databases/sun-appserv-samples:port=1527:user=APP:password=APP:connectionAttributes=;'create\\=true':host=127.0.0.1:classname=org.apache.derby.jdbc.EmbeddedXADataSource:resourcetype=javax.sql.XADataSource");
        parameterMap.add("DEFAULT", "my-external-db-service");

        invocation.parameters(parameterMap).execute();
        Assert.assertFalse(report.hasFailures());


/*
        //Create external service of type Database
        // asadmin create-external-service --servicetype=Database --configuration ip-address=127.0.0.1:databasename=sun-appserv-samples:port=1527:user=APP:password=APP:host=127.0.0.1:classname=org.apache.derby.jdbc.ClientXADataSource:resourcetype=javax.sql.XADataSource my-external-db-service
        parameterMap = new ParameterMap();
        parameterMap.add("servicetype", "Database");
        parameterMap.add("configuration", "ip-address=127.0.0.1:databasename=sun-appserv-samples:port=1527:user=APP:password=APP:host=127.0.0.1:classname=org.apache.derby.jdbc.ClientXADataSource:resourcetype=javax.sql.XADataSource");
        //parameterMap.add("configuration", "ip-address=127.0.0.1:databasename=${com.sun.aas.installRoot}/databases/sun-appserv-samples:port=1527:user=APP:password=APP:connectionAttributes=;'create\\=true':host=127.0.0.1:classname=org.apache.derby.jdbc.EmbeddedXADataSource:resourcetype=javax.sql.XADataSource");
        parameterMap.add("DEFAULT", "my-external-db-service");

        invocation.parameters(parameterMap).execute();

        System.out.println("Created external service 'my-external-db-service' :" + !report.hasFailures());
*/
        Assert.assertFalse(report.hasFailures());

        // Create shared service of type LB
        //asadmin create-shared-service --characteristics service-type=LB --configuration http-port=50080:https-port=50081:ssl-enabled=true --servicetype LB my-shared-lb-service
        invocation = commandRunner.getCommandInvocation("create-shared-service", report);
        parameterMap = new ParameterMap();
        parameterMap.add("servicetype", "LB");
        parameterMap.add("characteristics", "service-type=LB");
        parameterMap.add("configuration", "http-port=50080:https-port=50081:ssl-enabled=true");
        parameterMap.add("DEFAULT", "my-shared-lb-service");
        invocation.parameters(parameterMap).execute();

        System.out.println("Created shared service 'my-shared-lb-service' :" + !report.hasFailures());
        Assert.assertFalse(report.hasFailures());
        {
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
    }

    private void testSharedAndExternalService() {

        System.out.println("$$$$$$$$$$$$$ TEST SHARED AND EXTERNAL SERVICES $$$$$$$$$$$$$$$");
        ServiceLocator habitat = Globals.getDefaultHabitat();
        org.glassfish.api.admin.CommandRunner commandRunner = habitat.getService(org.glassfish.api.admin.CommandRunner.class);
        ActionReport report = habitat.getService(ActionReport.class);
        //Try stopping a shared service, referenced by the app. Should 'FAIL'

        org.glassfish.api.admin.CommandRunner.CommandInvocation invocation = commandRunner.getCommandInvocation("stop-shared-service", report);
        ParameterMap parameterMap = new ParameterMap();
        parameterMap.add("DEFAULT", "my-shared-lb-service");
        invocation.parameters(parameterMap).execute();

        System.out.print("Expected Failure message: " + report.getMessage());
        Assert.assertTrue(report.hasFailures());

        //Try deleting a shared service, referenced by the app. Should 'FAIL'
        report = habitat.getService(ActionReport.class);
        invocation = commandRunner.getCommandInvocation("delete-shared-service", report);
        parameterMap = new ParameterMap();
        parameterMap.add("DEFAULT", "my-shared-lb-service");
        invocation.parameters(parameterMap).execute();

        System.out.print("Expected Failure message: " + report.getMessage());
        Assert.assertTrue(report.hasFailures());


        //Try deleting a external service, referenced by the app. Should 'FAIL'
        invocation = commandRunner.getCommandInvocation("delete-external-service", report);
        parameterMap = new ParameterMap();
        parameterMap.add("DEFAULT", "my-external-db-service");
        invocation.parameters(parameterMap).execute();

        System.out.println("Expected Failure message: " + report.getMessage());
        Assert.assertTrue(report.hasFailures());

        invocation = commandRunner.getCommandInvocation("stop-shared-service", report);
        parameterMap = new ParameterMap();
        parameterMap.add("DEFAULT", "my-shared-lb-service");
        invocation.parameters(parameterMap).execute();

        Assert.assertTrue(report.hasFailures());
        System.out.print("Expected failure MSG: " + report.getMessage());

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

    private void deleteSharedAndExternalService() {
        ServiceLocator habitat = Globals.getDefaultHabitat();
        org.glassfish.api.admin.CommandRunner commandRunner = habitat.getService(org.glassfish.api.admin.CommandRunner.class);
        ActionReport report = habitat.getService(ActionReport.class);

        org.glassfish.api.admin.CommandRunner.CommandInvocation invocation =
                commandRunner.getCommandInvocation("delete-shared-service", report);
        ParameterMap parameterMap = new ParameterMap();
        parameterMap.add("DEFAULT", "my-shared-lb-service");
        invocation.parameters(parameterMap).execute();

        Assert.assertFalse(report.hasFailures());

        invocation = commandRunner.getCommandInvocation("delete-external-service", report);
        parameterMap = new ParameterMap();
        parameterMap.add("DEFAULT", "my-external-db-service");
        invocation.parameters(parameterMap).execute();

        Assert.assertFalse(report.hasFailures());
    }

}
