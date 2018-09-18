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

package org.glassfish.tests.paas.dns;

import junit.framework.Assert;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.embeddable.CommandResult;
import org.glassfish.embeddable.CommandRunner;
import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.glassfish.internal.api.Globals;
import org.junit.Test;
import org.glassfish.hk2.api.ServiceLocator;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.regex.*;
import org.glassfish.tests.paas.basetest.BaseTest;

/**
 * @author Shyamant Hegde
 */

public class BasicbookstoreDnsPaasTest {

	@Test
	public void test() throws Exception {

		// Bootstrap GlassFish DAS in embedded mode.
		GlassFish glassfish = bootstrap();

		// Deploy the Basic and bookstore  app and verify it.
		runTests(glassfish);

		

		// 5. Stop the GlassFish DAS
		glassfish.dispose();
	}

	private void get(String urlStr, String result) throws Exception {
		URL url = new URL(urlStr);
		URLConnection yc = url.openConnection();
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
		File basicArchive = new File(System.getProperty("basedir")
				+ "/basic_paas_sample.war"); // TODO :: use mvn apis to
		
		File bookArchive = new File(System.getProperty("basedir")
				+ "/bookstore.war");
		
		Assert.assertTrue(basicArchive.exists());
		Assert.assertTrue(bookArchive.exists());
		Deployer deployer = null;
		String firstappName = null;
		String secondappName = null;
		BaseTest firstBaseTest = new BaseTest(glassfish);
		BaseTest secondBaseTest = new BaseTest(glassfish);
		try {
		    	CreateDNSExternalService();
		    	CreateLbSharedService(glassfish);
			firstappName = firstBaseTest.deploy(basicArchive,"basic_paas_sample",null);
			

			System.err.println("Deployed [" + firstappName + "]");
			Assert.assertNotNull(firstappName);

			CommandRunner commandRunner = glassfish.getCommandRunner();
			CommandResult result = commandRunner.run("list-services");
			System.out.println("\nlist-services command output [ "
					+ result.getOutput() + "]");

			// 3. Access the app to make sure PaaS app is correctly provisioned.
			firstappName = firstappName.replaceAll("_","-");
			String HTTP_PORT = (System.getProperty("http.port") != null) ? System
		                    .getProperty("http.port") : "28080";
			
			get("http://" + firstappName +".hudson.com:" + HTTP_PORT
					+ "/BasicPaaSServlet",
					"Request headers from the request:");
			
			
			secondappName = secondBaseTest.deploy(bookArchive,"bookstore",null);
			System.err.println("Deployed [" + secondappName + "]");
			Assert.assertNotNull(secondappName);
			secondappName = secondappName.replaceAll("_","-");
			get("http://" + secondappName +".hudson.com:" + HTTP_PORT
		                    + "/BookStoreServlet",
		                    "Please wait while accessing the bookstore database.....");

		            get("http://"
		                    + secondappName +".hudson.com:"
		                    + HTTP_PORT
		                    + "/BookStoreServlet?title=Advanced+guide+for+developing+PaaS+components&authors=Shalini+M&price=100%24",
		                    "Here are the list of books available in our store:");

		            get("http://" + secondappName+".hudson.com:" + HTTP_PORT
		                    + "/BookStoreServlet",
		                    "Advanced guide for developing PaaS components");
		            get("http://" + firstappName +".hudson.com:" + HTTP_PORT
					+ "/BasicPaaSServlet",
					"Request headers from the request:"); 
		            if (secondappName != null) {
				secondBaseTest.undeploy();
				System.err.println("Undeployed [" + secondappName + "]");
		            }

		            get("http://" + firstappName +".hudson.com:" + HTTP_PORT
					+ "/BasicPaaSServlet",
					"Request headers from the request:"); 

			// 4. Undeploy the PaaS application . TODO :: use cloud-undeploy??
		} finally {
			if (firstappName != null) {
				firstappName = firstappName.replaceAll("-","_");

				firstBaseTest.undeploy();
				System.err.println("Undeployed [" + firstappName + "]");
				deleteSharedLbandDNSexternalSerivce();
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

    /* Creates LB as a shared service */
    public void CreateLbSharedService(GlassFish glassfish) throws Exception {
	ServiceLocator habitat = Globals.getDefaultHabitat();
	org.glassfish.api.admin.CommandRunner commandRunner = habitat
		.getService(org.glassfish.api.admin.CommandRunner.class);
	ActionReport report = habitat.getService(ActionReport.class);
	String template = checkMode(glassfish);
	if(template.equalsIgnoreCase("Native")){
	    template = "LBNative";
	}else if(template.equalsIgnoreCase("kvm")){
	    template = "apachemodjk";
	}else{
	    template = "otd-new";
	}
	org.glassfish.api.admin.CommandRunner.CommandInvocation invocation = commandRunner
		.getCommandInvocation("create-shared-service", report);
	ParameterMap parameterMap = new ParameterMap();
        parameterMap.add("template", template);
        parameterMap.add("configuration", "http-port=50080:https-port=50081:ssl-enabled=true:health-check-interval=60:health-check-timeout=10");
        parameterMap.add("servicetype", "LB");
        parameterMap.add("DEFAULT","hudson-shared-lb-service");
        invocation.parameters(parameterMap).execute();

        System.out.println("Created shared service 'hudson-shared-lb-service' :" + !report.hasFailures());
        Assert.assertFalse(report.hasFailures());

    }
    
    /*Create DNS external shared service*/
    
    public void CreateDNSExternalService(){
	
	ServiceLocator habitat = Globals.getDefaultHabitat();
	org.glassfish.api.admin.CommandRunner commandRunner = habitat
		.getService(org.glassfish.api.admin.CommandRunner.class);
	ActionReport report = habitat.getService(ActionReport.class);
	org.glassfish.api.admin.CommandRunner.CommandInvocation invocation = commandRunner
		.getCommandInvocation("create-external-service", report);
	ParameterMap parameterMap = new ParameterMap();
	parameterMap.add("servicetype", "DNS");
	parameterMap.add("configuration", "domain-name=hudson.com:dns-ip=10.178.214.173:dns-private-key-file-loc="+System.getenv("PAAS_TESTS_HOME")+"/basic-bookstore-dns/Kkey-glassfish.+157+05094.private");
	parameterMap.add("DEFAULT","hudson-dns-external-services");
	invocation.parameters(parameterMap).execute();

        System.out.println("Created external service 'hudson-dns-external-services' :" + !report.hasFailures());
        Assert.assertFalse(report.hasFailures());
	
    }
    
    /* Check the mode of test execution */
    public String checkMode(GlassFish glassfish) throws Exception {
	ArrayList params = new ArrayList();
	CommandResult result = null;
	CommandRunner commandRunner = glassfish.getCommandRunner();
	params.clear();
	params.add("--virtualization");
	params.add("Native");
	result = commandRunner.run("list-templates",
		(String[]) params.toArray(new String[params.size()]));
	if (result.getOutput().contains("Native")) {
	    return "Native";
	}
	params.clear();
	params.add("--virtualization");
	params.add("kvm");
	result = commandRunner.run("list-templates",
		(String[]) params.toArray(new String[params.size()]));
	if (result.getOutput().contains("apachemodjk")) {
	    return "kvm";
	} else {
	    return "ovm";
	}

    }
	
    /*Delete shared LB service*/
    private void deleteSharedLbandDNSexternalSerivce() {
        ServiceLocator habitat = Globals.getDefaultHabitat();
        org.glassfish.api.admin.CommandRunner commandRunner = habitat.getService(org.glassfish.api.admin.CommandRunner.class);
        ActionReport report = habitat.getService(ActionReport.class);
        //Try stopping a shared service, referenced by the app. Should 'FAIL'

        org.glassfish.api.admin.CommandRunner.CommandInvocation invocation = commandRunner.getCommandInvocation("delete-shared-service", report);
        ParameterMap parameterMap = new ParameterMap();
        parameterMap.add("DEFAULT", "hudson-shared-lb-service");
        invocation.parameters(parameterMap).execute();

        Assert.assertFalse(report.hasFailures());
        
        invocation = commandRunner.getCommandInvocation("delete-external-service", report);
        parameterMap = new ParameterMap();
        parameterMap.add("DEFAULT", "hudson-dns-external-services");
        invocation.parameters(parameterMap).execute();

        Assert.assertFalse(report.hasFailures());

    }
	
	

}
