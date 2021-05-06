/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package util;

import java.io.*;
import java.net.*;
import java.util.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

/** WebTestUtil.java
  * This program opens HttpURLconnection,sends the request to the
  * servlet , & receives the response from the servlet.
  * Using commandline args the user can specify for WebTestUtil
  * 1. test suite name
  * 2. host name
  * 3. port no
  * 4. context root of the servlet that is defined in web.xml
  * 5. url pattern of the servlet that is defined in web.xml
  *
  * @author      Sarada Kommalapati
  */


public class WebTestUtil {

    private SimpleReporterAdapter stat;

    private String testSuiteID;
    private String TEST_NAME;
    private String host;
    private String port;
    private String contextRoot;
    private String urlPattern;


    public WebTestUtil( String host, String port, String contextRoot , String urlPattern, String testSuiteID, SimpleReporterAdapter stat) {
        this.testSuiteID = testSuiteID;
        TEST_NAME = testSuiteID;
        this.host = host;
        this.port = port;
        this.contextRoot = contextRoot;
        this.urlPattern = urlPattern;
        this.stat = stat;
    }


    public void test( String c) throws Exception {
      this.test( c, "");
    }


    public void test( String c, String params) throws Exception {
        String EXPECTED_RESPONSE = c + ":pass";
        String TEST_CASE = TEST_NAME + c;
        String url = "http://" + host + ":" + port + contextRoot + "/";
        url = url + urlPattern + "?case=" + c;
        if ( (params != null) & (!params.trim().equals("")) ) {
            url = url + "&" + params.trim();
        }

        System.out.println("url="+url);

        HttpURLConnection conn = (HttpURLConnection)
            (new URL(url)).openConnection();
        int code = conn.getResponseCode();
        if (code != 200) {
            System.err.println("Unexpected return code: " + code);
            stat.addStatus(TEST_CASE, stat.FAIL);
        } else {
            InputStream is = conn.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = input.readLine()) != null) {
              // System.out.println("line="+line);
              if (line.contains(EXPECTED_RESPONSE)) {
                stat.addStatus(TEST_CASE, stat.PASS);
                break;
              }
            }

            if (line == null) {
              System.out.println("Unable to find " + EXPECTED_RESPONSE +
                                  " in the response");
            }
            stat.addStatus(TEST_CASE, stat.FAIL);
        }
    }

}


