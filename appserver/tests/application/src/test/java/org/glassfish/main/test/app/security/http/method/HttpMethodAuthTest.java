/*
 * Copyright (c) 2023 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.test.app.security.http.method;

import java.io.File;
import java.lang.System.Logger;
import java.net.URL;

import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.HttpClient10;
import org.glassfish.main.itest.tools.HttpClient10.HttpResponse;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static java.lang.System.Logger.Level.INFO;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.createFileUser;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertAll;


public class HttpMethodAuthTest {

    private static final Logger LOG = System.getLogger(HttpMethodAuthTest.class.getName());

    private static final String APP_NAME = "security-jmac-httpservlet";
    private static final String FILE_REALM_NAME = "file";

    private static final String USER_SHINGWAI_NAME = "shingwai";
    private static final String USER_SHINGWAI_PASSWORD = "password123";
    private static final String USER_SWCHAN_NAME = "swchan";
    private static final String USER_SWCHAN_PASSWORD = "p987123";

    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();

    @TempDir
    private static File tempDir;
    private static File warFile;


    @BeforeAll
    public static void prepareDeployment() {
        createFileUser(FILE_REALM_NAME, USER_SHINGWAI_NAME, USER_SHINGWAI_PASSWORD, "employee");
        createFileUser(FILE_REALM_NAME, USER_SWCHAN_NAME, USER_SWCHAN_PASSWORD, "staff");

        WebArchive webArchive = ShrinkWrap.create(WebArchive.class)
            .addClass(HttpMethodTestServlet.class)
            .addAsWebInfResource(HttpMethodTestServlet.class.getPackage(), "web.xml", "web.xml")
            .addAsWebInfResource(HttpMethodTestServlet.class.getPackage(), "sun-web.xml", "sun-web.xml");

        LOG.log(INFO, webArchive.toString(true));

        warFile = new File(tempDir, APP_NAME + ".war");
        webArchive.as(ZipExporter.class).exportTo(warFile, true);
        assertThat(ASADMIN.exec("deploy", "--target", "server", warFile.getAbsolutePath()), asadminOK());
    }


    @AfterAll
    public static void cleanup() {
        ASADMIN.exec("undeploy", APP_NAME);
        ASADMIN.exec("delete-file-user", "--authrealmname", FILE_REALM_NAME, "--target", "server", USER_SHINGWAI_NAME);
        ASADMIN.exec("delete-file-user", "--authrealmname", FILE_REALM_NAME, "--target", "server", USER_SWCHAN_NAME);
        delete(warFile);
    }


    @Test
    void testServletManagerFoo() throws Exception {
        HttpClient10 client = new HttpClient10(new URL("http://localhost:8080/" + APP_NAME + "/TestServlet"),
            USER_SHINGWAI_NAME, USER_SHINGWAI_PASSWORD);
        HttpResponse responseFoo = client.send("FOO", null);
        assertAll(
            () -> assertThat(responseFoo.responseLine, equalTo("HTTP/1.1 200 OK")),
            () -> assertThat(responseFoo.body, equalTo("doFoo with shingwai"))
        );
    }


    @Test
    void testServletManagerGet() throws Exception {
        HttpClient10 client = new HttpClient10(new URL("http://localhost:8080/" + APP_NAME + "/TestServlet"),
            USER_SHINGWAI_NAME, USER_SHINGWAI_PASSWORD);
        HttpResponse responseGet = client.send("GET", null);
        assertAll(
            () -> assertThat(responseGet.responseLine, equalTo("HTTP/1.1 200 OK")),
            () -> assertThat(responseGet.body, equalTo("doGet with shingwai")));
    }


    @Test
    void testServlet2ManagerFoo() throws Exception {
        HttpClient10 client = new HttpClient10(new URL("http://localhost:8080/" + APP_NAME + "/TestServlet2"),
            USER_SHINGWAI_NAME, USER_SHINGWAI_PASSWORD);
        HttpResponse responseFoo = client.send("FOO", null);
        assertAll(
            () -> assertThat(responseFoo.responseLine, equalTo("HTTP/1.1 200 OK")),
            () -> assertThat(responseFoo.body, equalTo("doFoo with shingwai"))
        );
    }


    @Test
    void testServletEmployeeFoo() throws Exception {
        HttpClient10 client = new HttpClient10(new URL("http://localhost:8080/" + APP_NAME + "/TestServlet"),
            USER_SWCHAN_NAME, USER_SWCHAN_PASSWORD);
        HttpResponse responseFoo = client.send("FOO", null);
        assertAll(
            () -> assertThat(responseFoo.responseLine, equalTo("HTTP/1.1 403 Forbidden")),
            () -> assertThat(responseFoo.body, stringContainsInOrder("Error report", "HTTP Status 403 - Forbidden"))
        );
    }


    @Test
    void testServlet2EmployeeFoo() throws Exception {
        HttpClient10 client = new HttpClient10(new URL("http://localhost:8080/" + APP_NAME + "/TestServlet2"),
            USER_SWCHAN_NAME, USER_SWCHAN_PASSWORD);
        HttpResponse responseFoo = client.send("FOO", null);
        assertAll(
            () -> assertThat(responseFoo.responseLine, equalTo("HTTP/1.1 403 Forbidden")),
            () -> assertThat(responseFoo.body, stringContainsInOrder("Error report", "HTTP Status 403 - Forbidden"))
        );
    }

    private static void delete(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }
}
