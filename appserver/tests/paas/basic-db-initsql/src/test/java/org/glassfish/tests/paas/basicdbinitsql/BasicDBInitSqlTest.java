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

package org.glassfish.tests.paas.basicdbinitsql;

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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Shalini M
 */

public class BasicDBInitSqlTest {

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
				+ "/target/basic_db_initsql_paas_sample.war"); // TODO :: use
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

			CommandRunner commandRunner = glassfish.getCommandRunner();
			CommandResult result = commandRunner.run("list-services");
			System.out.println("\nlist-services command output [ "
					+ result.getOutput() + "]");

			// 3. Access the app to make sure PaaS app is correctly provisioned.
			String HTTP_PORT = (System.getProperty("http.port") != null) ? System
					.getProperty("http.port") : "28080";
			String instanceIP = getLBIPAddress(glassfish);

			get("http://" + instanceIP + ":" + HTTP_PORT
					+ "/basic_db_initsql_paas_sample/BasicDBInitSqlServlet",
					"Customer ID");

            testListServices();

			// 4. Undeploy the PaaS application .
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

private void testListServices() {
        ServiceLocator habitat = Globals.getDefaultHabitat();
        org.glassfish.api.admin.CommandRunner commandRunner = habitat.getService(org.glassfish.api.admin.CommandRunner.class);


        //Testing for the '--output' option of lst-services sub-commands
        {
            List<String> outputOptions = new ArrayList<String>();
            outputOptions.add("SERVICE-NAME");
            outputOptions.add("VM-ID");
            outputOptions.add("SERVER-TYPE");
            ActionReport report = habitat.getService(ActionReport.class);
            org.glassfish.api.admin.CommandRunner.CommandInvocation invocation = commandRunner.getCommandInvocation("list-services", report);
            ParameterMap parameterMap = new ParameterMap();
            parameterMap.add("output", "service-name,vm-id,server-type");
            invocation.parameters(parameterMap).execute();
            List<Map<String, String>> list = (List<Map<String, String>>) report.getExtraProperties().get("list");
            Map<String, String> map = list.get(0);
            Set set = map.keySet();
            outputOptions.removeAll(set);
            boolean isEmpty = outputOptions.isEmpty();
            System.out.println("list-services --output option test passed:: " + isEmpty);
            Assert.assertTrue(isEmpty);
        }

        //Testing for the '--key' option of lst-services sub-commands
        {
            String key = "VM-ID";
            ActionReport report = habitat.getService(ActionReport.class);
            org.glassfish.api.admin.CommandRunner.CommandInvocation invocation = commandRunner.getCommandInvocation("list-services", report);
            ParameterMap parameterMap = new ParameterMap();
            parameterMap.add("key", key);
            parameterMap.add("output", key);
            invocation.parameters(parameterMap).execute();
            List<Map<String, String>> list = (List<Map<String, String>>) report.getExtraProperties().get("list");
            ListIterator listIterator = list.listIterator();
            List<String> valueList = new ArrayList<String>();
            Map<String, String> map;
            while (listIterator.hasNext()) {
                map = (Map<String, String>) listIterator.next();
                valueList.add(map.get(key));
            }
            boolean isSorted = isSortedList(valueList);
            System.out.println("list-services --key option test passed:: " + isSorted);
            Assert.assertTrue(isSorted);

        }

        //Testing for the '--type' option of lst-services sub-commands
        {
            String type = "DATABASE";
            ActionReport report = habitat.getService(ActionReport.class);
            org.glassfish.api.admin.CommandRunner.CommandInvocation invocation = commandRunner.getCommandInvocation("list-services", report);
            ParameterMap parameterMap = new ParameterMap();
            parameterMap.add("type", type);
            parameterMap.add("output", "SERVER-TYPE");
            invocation.parameters(parameterMap).execute();
            List<Map<String, String>> list = (List<Map<String, String>>) report.getExtraProperties().get("list");
            ListIterator listIterator = list.listIterator();
            List<String> valueList = new ArrayList<String>();
            Map<String, String> map;
            String typeFound = null;
            boolean onlyTypeFound = false;
            while (listIterator.hasNext()) {
                map = (Map<String, String>) listIterator.next();
                typeFound = (String) map.get("SERVER-TYPE");
                if (type.equals(typeFound)) {
                    onlyTypeFound = true;
                } else {
                    onlyTypeFound = false;
                    break;
                }
            }
            if (valueList.isEmpty()) {
                onlyTypeFound = true;
            }

            System.out.println("list-services --type option test passed:: " + onlyTypeFound);
            Assert.assertTrue(onlyTypeFound);

        }

        //Testing for the '--scope' option of lst-services sub-commands
        //Here, the war deployed is 'basic_db_initsql_paas_sample.war', hence using it as the appname.
        {
            String scope = "application";
            ActionReport report = habitat.getService(ActionReport.class);
            org.glassfish.api.admin.CommandRunner.CommandInvocation invocation = commandRunner.getCommandInvocation("list-services", report);
            ParameterMap parameterMap = new ParameterMap();
            parameterMap.add("scope", scope);
            parameterMap.add("output", "SCOPE");
            parameterMap.add("appname", "basic_db_initsql_paas_sample");
            invocation.parameters(parameterMap).execute();
            List<Map<String, String>> list = (List<Map<String, String>>) report.getExtraProperties().get("list");
            ListIterator listIterator = list.listIterator();
            List<String> valueList = new ArrayList<String>();
            Map<String, String> map;
            String scopeFound = null;
            boolean onlyScopeFound = false;
            while (listIterator.hasNext()) {
                map = (Map<String, String>) listIterator.next();
                scopeFound = (String) map.get("SCOPE");
                if (scope.equals(scopeFound)) {
                    onlyScopeFound = true;
                } else {
                    onlyScopeFound = false;
                    break;
                }
            }
            if (valueList.isEmpty()) {
                onlyScopeFound = true;
            }

            System.out.println("list-services --scope option test passed:: " + onlyScopeFound);
            Assert.assertTrue(onlyScopeFound);

        }

        //  test the option --terse=false.
        {

            ActionReport report = habitat.getService(ActionReport.class);
            org.glassfish.api.admin.CommandRunner.CommandInvocation invocation = commandRunner.getCommandInvocation("list-services", report);
            ParameterMap parameterMap = new ParameterMap();
            parameterMap.add("output", "service-name,vm-id,server-type");
            parameterMap.add("terse", "true");
            invocation.parameters(parameterMap).execute();
            List<Map<String, String>> list = (List<Map<String, String>>) report.getExtraProperties().get("list");
            boolean headersNotFound = false;
            for (Map<String, String> map : list) {
                headersNotFound = false;
                Set<String> headers = map.keySet();
                for (String header : headers) {
                    if ("".equals(header)) {
                        headersNotFound = true;
                    } else {
                        headersNotFound = false;
                        break;
                    }
                }
                if (!headersNotFound) {
                    break;
                }
            }
            System.out.println("list-services --terse=true option test passed:: " + headersNotFound);
            Assert.assertTrue(headersNotFound);


        }


        // test --type option
        {
            String typeValue="Javaee";
            ActionReport report = habitat.getService(ActionReport.class);
            org.glassfish.api.admin.CommandRunner.CommandInvocation invocation = commandRunner.getCommandInvocation("list-services", report);
            ParameterMap parameterMap = new ParameterMap();
            parameterMap.add("output", "server-type");
            parameterMap.add("type",typeValue);
            invocation.parameters(parameterMap).execute();
            List<Map<String, String>> list = (List<Map<String, String>>) report.getExtraProperties().get("list");
            boolean otherTypeFound = false;
            for (Map<String, String> map : list) {
                 String value=  map.get("SERVER-TYPE");
                 if(!value.equalsIgnoreCase(typeValue)){
                     otherTypeFound=true;
                     break;
                 }
            }
            System.out.println("list-services --type option test passed:: " + !otherTypeFound);
            Assert.assertFalse(otherTypeFound);
        }


    }

    private boolean isSortedList(List list) {
        ListIterator list_iter = list.listIterator();
        if (!list_iter.hasNext()) {
            return true;
        }
        String t = (String) list_iter.next();
        while (list_iter.hasNext()) {
            String t2 = (String) list_iter.next();
            if (t.compareTo(t2) > 0) {
                return false;
            }
            t = t2;
        }
        return true;


    }

}
