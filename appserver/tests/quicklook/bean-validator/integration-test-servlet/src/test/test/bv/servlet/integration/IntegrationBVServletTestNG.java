/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package test.bv.servlet.integration;

import org.testng.annotations.*;
import org.testng.Assert;

import java.io.*;
import java.net.*;

public class IntegrationBVServletTestNG {

    private static final String TEST_NAME =
        "bv-servlet-integration";
   
    private String strContextRoot="integration-test-servlet";

    static String result = "";
    String host=System.getProperty("http.host");
    String port=System.getProperty("http.port");

    public IntegrationBVServletTestNG() {
        result = null;
    }
    
    
           
    /*
     *If two asserts are mentioned in one method, then last assert is taken in
     *to account.
     *Each method can act as one test within one test suite
     */


    //@Parameters({ "host", "port", "contextroot" })
    @Test(groups ={ "pulse"} ) // test method
    //public void webtest(String host, String port, String contextroot) throws Exception{
    public void executeServlet() throws Exception{
        
        try{

            String testurl = "http://" + host + ":" + port + "/" + strContextRoot + "/test";
            //System.out.println("URL is: "+testurl);
            URL url = new URL(testurl);
            //echo("Connecting to: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            int responseCode = conn.getResponseCode();

            InputStream is = conn.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(is));

            String line = null;
            boolean result = false;
            String testLine = null;
            String[] regexesToFind = {
                "(?s)(?m).*Obtained ValidatorFactory: org.hibernate.validation.(internal.)*engine.ValidatorFactoryImpl.*",
                "(?s)(?m).*case1: No ConstraintViolations found.*",
                "(?s)(?m).*case2: caught IllegalArgumentException.*",
                "(?s)(?m).*case3: ConstraintViolation: message: may not be null propertyPath: listOfString.*",
                "(?s)(?m).*case3: ConstraintViolation: message: may not be null propertyPath: lastName.*",
                "(?s)(?m).*case3: ConstraintViolation: message: may not be null propertyPath: firstName.*",
                "(?s)(?m).*case4: No ConstraintViolations found.*"
            };
            final int len = regexesToFind.length;
            int i;
            Boolean regexesFound[] = new Boolean[len];

            while ((line = input.readLine()) != null) {
                // for each line in the input, loop through each of the 
                // elements of regexesToFind.  At least one must match.
                boolean found = false;
                for (i = 0; i < len; i++) {
                    if (found = line.matches(regexesToFind[i])) {
                        regexesFound[i] = Boolean.TRUE;
                    }
                }
            }
            
            boolean foundMissingRegexMatch = false;
            String errorMessage = null;
            for (i = 0; i < len; i++) {
                if (null == regexesFound[i] ||
                    Boolean.FALSE == regexesFound[i]) {
                    foundMissingRegexMatch = true;
                    errorMessage = "Unable to find match for regex " + 
                            regexesToFind[i] + " in output from request to " + testurl;
                    break;
                }
            }
            
            Assert.assertTrue(!foundMissingRegexMatch, errorMessage);

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e);
        }

    }

    public static void echo(String msg) {
        System.out.println(msg);
    }

}
