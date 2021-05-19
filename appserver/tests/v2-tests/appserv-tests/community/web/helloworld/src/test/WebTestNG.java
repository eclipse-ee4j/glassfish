/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package test.web.jsp.hello;

import org.testng.annotations.Configuration;
import org.testng.annotations.ExpectedExceptions;
import org.testng.annotations.Test;
import org.testng.annotations.Parameters;
import org.testng.Assert;

import java.io.*;
import java.net.*;
import java.util.*;

public class WebTestNG {

    private static final String TEST_NAME =
        "simple-webapp-jspservlet-noresource";

    private static final String EXPECTED_RESPONSE =
        "JSP Test Page";

    private String strContextRoot="hellojsp";

    static String result = "";
    /*
     *If two asserts are mentioned in one method, then last assert is taken in
     *to account.
     *Each method can act as one test within one test suite
     */


    //@Parameters({ "host", "port", "contextroot" })
    @Test(groups ={ "pulse"} ) // test method
    //public void webtest(String host, String port, String contextroot) throws Exception{
    public void webtest() throws Exception{

        try{
            String host=System.getProperty("host");
            String port=System.getProperty("port");
            String contextRoot=System.getProperty("contextroot");

        String testurl = "http://" + host  + ":" + port + "/"+ contextRoot + "/hello.jsp";
        System.out.println("URL is: "+testurl);
        URL url = new URL(testurl);
        echo("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();

        Assert.assertEquals(responseCode, 200);

        InputStream is = conn.getInputStream();
        BufferedReader input = new BufferedReader(new InputStreamReader(is));

        String line = null;
        boolean result=false;
        String testLine = null;
        while ((line = input.readLine()) != null) {
            if(line.indexOf(EXPECTED_RESPONSE)!=-1){
                result=true;
             testLine = line;
           System.out.println(testLine);
            }

        }

        Assert.assertEquals(result, true,"Unexpected HTML");


        }catch(Exception e){
            e.printStackTrace();
            throw new Exception(e);
        }

    }

    @Test(groups ={ "pulse"} ) // test method
    public void dummyTest(){
        Assert.assertEquals(false, true,"Dummy Failure");

    }

    public static void echo(String msg) {
        System.out.println(msg);
    }

}
