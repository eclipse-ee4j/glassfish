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

package org.glassfish.main.test.app.security.servlet.hello;


import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import org.glassfish.main.itest.tools.ITestBase;
import org.glassfish.main.test.app.security.servlet.basicauth.BasicAuthTest;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Simple test for basic WAR containing one JSP, one Servlet and one static HTML resource.
 *
 * Each resource (HTML, JSP, Servlet) is invoked as a separate test.
 *
 */
public class HelloTest extends ITestBase {


    @BeforeAll
    public void deploy() throws Exception {
        doDeploy(
            ShrinkWrap.create(WebArchive.class, BasicAuthTest.class.getSimpleName() + "WebApp")
                      .addClass(HelloServlet.class)
                      .addAsWebResource(
                          HelloTest.class.getPackage(), "first.html", "first.html")
                      .addAsWebResource(
                          HelloTest.class.getPackage(), "hello.jsp", "hello.jsp")
                      .addAsWebInfResource(
                          HelloTest.class.getPackage(), "web.xml", "web.xml")
                      .addAsWebInfResource(
                          HelloTest.class.getPackage(), "sun-web.xml", "sun-web.xml"));
    }

    /*
     * If two asserts are mentioned in one method, then last assert is taken in to account. Each method can act as one test
     * within one test suite
     */
    @Test
    public void simpleJSPTestPage() throws Exception {

        try {
            int responseCode = getResponseCodeFromServer("/hello.jsp");
            assertEquals(401, responseCode, "Anonymous Client acess was Allowed, security-constraint not Enforced");

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e);
        }

    }

    @Test
    public void staticHTMLPageTest() throws Exception {
        try {
            int responseCode = getResponseCodeFromServer("/first.html");
            assertEquals(401, responseCode, "Anonymous Client acess was Allowed, security-constraint not Enforced");

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e);
        }

    }

    @Test
    public void simpleServletTest() throws Exception {
        try {
            int responseCode = getResponseCodeFromServer("/simpleservlet");
            assertEquals(401, responseCode, "Anonymous Client acess was Allowed, security-constraint not Enforced");

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e);
        }
    }

    private int getResponseCodeFromServer(String resource )throws Exception {
        String testurl = "http://" + host + ":" + port + "/" + appName + resource;
        URL url = new URI(testurl).toURL();

        echo("Connecting to: " + url.toString());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();

        return connection.getResponseCode();
    }


}
