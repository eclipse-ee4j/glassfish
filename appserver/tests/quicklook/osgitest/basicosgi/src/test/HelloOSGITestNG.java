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

package test.osgi.hello;

import com.sun.appserv.test.AdminBaseDevTest;
import org.testng.annotations.Configuration;
import org.testng.annotations.ExpectedExceptions;
import org.testng.annotations.Test;
import org.testng.annotations.*;
import org.testng.Assert;
import java.io.*;
import java.lang.String;
import java.lang.System;
import java.net.*;
import java.util.*;


public class HelloOSGITestNG extends AdminBaseDevTest{

    @Override
    protected String getTestDescription() {
        return "OSGI simple Test";
    }

    final String tn = "OSGI";
    private static final String TEST_NAME = "osgi-webapp-test";
    static String BASEDIR = System.getProperty("BASEDIR");
    public boolean retStatus = false;
    final String cname = "osgi";
    final String flag = "true";
    final String options = "UriScheme=webBundle:Bundle-SymbolicName=bar:Import-Package=jakarta.servlet;jakarta.servlet.http:Web-ContextPath=/osgitest";

    private String strContextRoot="osgitest";

    static String result = "";
    String host=System.getProperty("http.host");
    String port=System.getProperty("http.port");


    @Test(groups ={ "osgi"} ) // test method
    public void simpleOSGIDeployTest() throws Exception{

        // deploy web application.
        File webapp = new File(BASEDIR+"/dist/basicosgi", "osgitest.war");
        retStatus = report(tn + "deploy", asadmin("deploy", "--type", cname, "--properties", options, webapp.getAbsolutePath()));
        Assert.assertEquals(retStatus, true, "App deployment failed ...");

    }

    @Test(groups ={ "osgi"},dependsOnMethods = { "simpleOSGIDeployTest" } ) // test method
     public void simpleJSPTestPage() throws Exception{

        try{
         Thread.currentThread().sleep(5000);

        String testurl = "http://" + host  + ":" + port + "/"+ strContextRoot + "/hello.jsp";
        URL url = new URL(testurl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        InputStream is = conn.getInputStream();
        BufferedReader input = new BufferedReader(new InputStreamReader(is));

        String line = null;
        boolean result=false;
        String testLine = null;
        String EXPECTED_RESPONSE ="JSP Test Page";
        while ((line = input.readLine()) != null) {
            if(line.indexOf(EXPECTED_RESPONSE)!=-1){
                result=true;
             testLine = line;
            }
        }
        Assert.assertEquals(result, true,"Unexpected HTML");
        }catch(Exception e){
            e.printStackTrace();
            throw new Exception(e);
        }

    }

    @Test(groups ={ "osgi"},dependsOnMethods = { "simpleJSPTestPage" }  ) // test method
    public void simpleServletTest() throws Exception{
         try{
        String testurl = "http://" + host  + ":" + port + "/"+ strContextRoot + "/simpleservlet";
        URL url = new URL(testurl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        InputStream is = conn.getInputStream();
        BufferedReader input = new BufferedReader(new InputStreamReader(is));

        String line = null;
        boolean result=false;
        String testLine = null;
        while ((line = input.readLine()) != null) {
            if(line.indexOf("Sample Application Servlet")!=-1){
                result=true;
             testLine = line;
            }
        }
        Assert.assertEquals(result, true,"Unexpected HTML");
        }catch(Exception e){
            e.printStackTrace();
            throw new Exception(e);
        }

    }


    @Test(groups ={ "osgi"},dependsOnMethods = { "simpleServletTest" } ) // test method
    public void simpleOSGIUnDeployTest() throws Exception{
        // undeploy web application.
         retStatus = report(tn + "undeploy", asadmin("undeploy","osgitest"));
         Assert.assertEquals(retStatus, true, "App Undeployment failed ...");;

    }

}
