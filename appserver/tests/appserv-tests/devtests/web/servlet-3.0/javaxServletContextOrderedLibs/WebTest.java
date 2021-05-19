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

import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/**
 * Unit test for jakarta.servlet.context.orderedLibs ServletContext
 * attribute.
 *
 * This unit test creates and deploys a WAR file that bundles two web
 * fragments with names webFragment1 and webFragment2, respectively.
 * Either fragment declares a ServletContextListener in its
 * web-fragment.xml descriptor file.
 *
 * The main web.xml declares an absolute ordering of the two web fragments,
 * with webFragment2 listed first, followed by webFragment1.
 *
 * Either ServletContextListener checks for the presence and contents of the
 * jakarta.servlet.context.orderedLibs ServletContext attribute and, if
 * satisfied, registers a Servlet.
 *
 * The client then accesses each of the Servlets. If it fails to access either
 * Servlet, it reports an error, causing the test to fail.
 */
public class WebTest {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME =
        "javax-servlet-context-orderedLibs";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for " +
            "jakarta.servlet.context.orderedLibs ServletContext attribute");

        try {
            new WebTest(args).doTest();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    private void doTest() throws Exception {

        String url = "http://" + host + ":" + port + contextRoot +
            "/webFragment1Servlet";
        HttpURLConnection conn = (HttpURLConnection)
            (new URL(url)).openConnection();
        int code = conn.getResponseCode();
        if (code != 200) {
            throw new Exception("Unexpected return code: " + code);
        }

        url = "http://" + host + ":" + port + contextRoot +
            "/webFragment2Servlet";
        conn = (HttpURLConnection) (new URL(url)).openConnection();
        code = conn.getResponseCode();
        if (code != 200) {
            throw new Exception("Unexpected return code: " + code);
        }

    }
}
