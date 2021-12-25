/*
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
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

package test.web.numberguess;

import org.testng.annotations.Configuration;
import org.testng.annotations.ExpectedExceptions;
import org.testng.annotations.Test;
import org.testng.annotations.*;
import org.testng.Assert;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Simple TestNG client for basic WAR
 *
 */
public class NumberGuessTestNG {

    private String strContextRoot = "numberguess";

    static String result = "";
    String m_host = "";
    String m_port = "";

    @BeforeMethod
    public void beforeTest() {
        m_host = System.getProperty("http.host");
        m_port = System.getProperty("http.port");
    }

    @Test(groups = { "pulse" }) // test method
    public void appDeployedFirstPagetest() throws Exception {

        try {
            HttpURLConnection conn = (HttpURLConnection) 
                new URL("http://" + m_host + ":" + m_port + "/" + strContextRoot + "/home.xhtml").openConnection();
            conn.connect();
            int responseCode = conn.getResponseCode();

            BufferedReader input = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = null;
            boolean result = false;
            String testLine = null;
            while ((line = input.readLine()) != null) {
                if (line.indexOf("thinking of a number between") != -1) {
                    result = true;
                    testLine = line;
                }
            }
            Assert.assertEquals(result, true, "Unexpected HTML");
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e);
        }
    }
}
