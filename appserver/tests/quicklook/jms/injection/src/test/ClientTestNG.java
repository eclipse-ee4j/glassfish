/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package test.jms.injection;

import java.io.*;
import java.net.*;

import jakarta.ejb.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.testng.annotations.Configuration;
import org.testng.annotations.ExpectedExceptions;
import org.testng.annotations.Test;
import org.testng.annotations.*;
import org.testng.Assert;

public class ClientTestNG {

    private static final String TEST_NAME = "jms-injection";
    static String result = "";
    private String strContextRoot = "/injection";
    String host=System.getProperty("http.host");
    String port=System.getProperty("http.port");

    private static String appName = "app";

   @Test
   public void testRequestScopedJMSContextInjection() throws Exception {
        try {
            boolean result = test("requestScope");
            Assert.assertEquals(result, true, "Unexpected Results");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    // @Test // - TODO: ENABLE AGAIN WHEN CAUSE OF FAILURE ON ECLIPSE CI FOUND (works locally)
    public void testTransactionScopedJMSContextInjection() throws Exception {
        try {
            boolean result = test("transactionScope");
            Assert.assertEquals(result, true, "Unexpected Results");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private boolean test(String c) throws Exception {
        String EXPECTED_RESPONSE = "Test:Pass";
        String TEST_CASE = TEST_NAME + ":" + c;
        boolean result=false;
        String url = "http://" + host + ":" + port + strContextRoot + "/test?tc=" + c;

        HttpURLConnection conn = (HttpURLConnection) (new URL(url)).openConnection();
        int code = conn.getResponseCode();
        if (code != 200) {
            System.err.println("Unexpected return code: " + code);
        } else {
            InputStream is = conn.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = input.readLine()) != null) {
                if (line.contains(EXPECTED_RESPONSE)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }
}
