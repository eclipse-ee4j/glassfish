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

package test.security.basicauth;

import java.lang.*;
import java.io.*;
import java.net.*;
import org.testng.annotations.Test;
import org.testng.Assert;

/**
 * A simple Web BASIC auth test.
 *
 */
@Test (groups={"pulse"})
public class BasicAuthTestNG {
    
    
    private static final String TEST_NAME =
        "security-basicauth-web";

    private String strContextRoot="basicauth";

    static String result = "";
    String host=System.getProperty("http.host");
    String portS=System.getProperty("http.port");
    int port = new Integer(portS).intValue();

    String testName;
        
    /**
     * Must be invoked with (host,port) args.
     * Nothing else is parameterized, this is intended as
     * throwaway after the SQE web test framework exists.
     * User/authorization info is hardcoded and must match
     * the values in descriptors and build.xml.
     *
     */
    @Test (groups = {"pulse"})
    public void testAuthRoleMappedUser() throws Exception {

       // System.out.println("Host ["+host+"] port ("+port+")");

        // GET with a user who maps directly to role
        testName="BASIC auth: Role Mapped User, testuser3";
        //log(testName);
        try {

            String result="RESULT: principal: testuser3";
            goGet(host, port, result,
                  "Authorization: Basic dGVzdHVzZXIzOnNlY3JldA==\n");
            Assert.assertTrue(true, testName);
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            Assert.assertFalse(true, testName);
        }
    }
    
    @Test (groups = {"pulse"})
    public void testAuthGroupMappedUser() {
        
        // GET with a user who maps through group
        testName="BASIC auth: Group mapped user, testuser42";
        //log(testName);
        try {

            String result="RESULT: principal: testuser42";
            goGet(host, port, result,
                  "Authorization: Basic dGVzdHVzZXI0MjpzZWNyZXQ=\n");
            Assert.assertTrue(true, testName);
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            Assert.assertFalse(true, testName);
        }
    }
    
    @Test (groups = {"pulse"})
    public void testAuthNotAuthorizedUser() {

        // GET with a valid user who is not authorized
        testName="BASIC auth: Not authorized user, testuser42";
        //log(testName);
        try {

            String result="HTTP/1.1 403";
            goGet(host, port, result,
                  "Authorization: Basic ajJlZTpqMmVl\n");
            Assert.assertTrue(true, testName);
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            Assert.assertFalse(true, testName);
        }
    }
    
    @Test (groups = {"pulse"}) 
    public void testAuthNotValidPassword() {

        // GET with a valid user,bad password
        testName="BASIC auth: Valid user and invalid password";
        //log(testName);
        try {

            String result="HTTP/1.1 401";
            goGet(host, port, result,
                  "Authorization: Basic ajJlZTo=\n");
            Assert.assertTrue(true, testName);
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            Assert.assertFalse(true, testName);
        }

    }

    /**
     * Connect to host:port and issue GET with given auth info.
     * This is hardcoded to expect the output that is generated
     * by the Test.jsp used in this test case.
     *
     */
    private static void goGet(String host, int port,
                              String result, String auth)
         throws Exception
    {
        Socket s = new Socket(host, port);
        OutputStream os = s.getOutputStream();

        os.write("GET /basicauth/Test.jsp HTTP/1.0\n".getBytes());
        os.write(auth.getBytes());
        os.write("\n".getBytes());
        
        InputStream is = s.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        String line = null;

        while ((line = bis.readLine()) != null) {
            if (line.indexOf(result) != -1) {
                //System.out.println("  Found: "+line);
                s.close();
                return;
            }
        }

        s.close();
        throw new Exception("String not found: "+result);
    }
    
    private void log(String mesg) {
        System.out.println(mesg);
    }
  
}
