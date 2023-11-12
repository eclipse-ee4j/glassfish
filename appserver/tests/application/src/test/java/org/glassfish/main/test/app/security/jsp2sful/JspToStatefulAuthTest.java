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

package org.glassfish.main.test.app.security.jsp2sful;

import java.io.File;
import java.lang.System.Logger;
import java.net.HttpURLConnection;
import java.net.URL;

import org.glassfish.main.itest.tools.FormAuthHttpClient;
import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static java.lang.System.Logger.Level.INFO;
import static org.glassfish.main.itest.tools.FormAuthHttpClient.readResponseBody;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.createFileUser;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;


public class JspToStatefulAuthTest {

    private static final Logger LOG = System.getLogger(JspToStatefulAuthTest.class.getName());

    private static final String APP_NAME = "security-jsp2sful";
    private static final String FILE_REALM_NAME = "file";

    private static final String GROUP_STAFF = "staff";
    private static final String GROUP_EMPLOYEE = "employee";

    private static final String USER_SHINGWAI_NAME = "shingwai";
    private static final String USER_SHINGWAI_PASSWORD = "12345";

    private static final String USER_SWCHAN_NAME = "swchan";
    private static final String USER_SWCHAN_PASSWORD = "a15505";

    private static final String USER_WEBUSER_NAME = "webuser";
    private static final String USER_WEBUSER_PASSWORD = "wu9997890";

    private static final String USER_NOAUTHUSER_NAME = "noauthuser";
    private static final String USER_NOAUTHUSER_PASSWORD = "xx5550554";

    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();

    @TempDir
    private static File tempDir;
    private static File earFile;

    private static URL baseUrl;


    @BeforeAll
    public static void prepareDeployment() throws Exception {
        createFileUser(FILE_REALM_NAME, USER_SHINGWAI_NAME, USER_SHINGWAI_PASSWORD, GROUP_EMPLOYEE);
        createFileUser(FILE_REALM_NAME, USER_SWCHAN_NAME, USER_SWCHAN_PASSWORD, GROUP_STAFF);
        createFileUser(FILE_REALM_NAME, USER_WEBUSER_NAME, USER_WEBUSER_PASSWORD, GROUP_EMPLOYEE);
        createFileUser(FILE_REALM_NAME, USER_NOAUTHUSER_NAME, USER_NOAUTHUSER_PASSWORD, GROUP_EMPLOYEE);

        final WebArchive webArchive = ShrinkWrap.create(WebArchive.class, APP_NAME + "-web.war")
            .addAsWebResource(ProfileInfoBean.class.getPackage(), "index.jsp", "index.jsp")
            .addAsWebResource(ProfileInfoBean.class.getPackage(), "login.jsp", "login.jsp")
            .addAsWebResource(ProfileInfoBean.class.getPackage(), "error.html", "error.html")
            .addAsWebInfResource(ProfileInfoBean.class.getPackage(), "web.xml", "web.xml")
            .addAsWebInfResource(ProfileInfoBean.class.getPackage(), "sun-web.xml", "sun-web.xml");
        LOG.log(INFO, webArchive.toString(true));

        final JavaArchive ejbArchive = ShrinkWrap.create(JavaArchive.class, APP_NAME + "-ejb.jar")
            .addClasses(ProfileInfoBean.class, ProfileInfoHome.class, ProfileInfoRemote.class)
            .addAsManifestResource(ProfileInfoBean.class.getPackage(), "ejb-jar.xml", "ejb-jar.xml")
            .addAsManifestResource(ProfileInfoBean.class.getPackage(), "sun-ejb-jar.xml", "sun-ejb-jar.xml");
        LOG.log(INFO, ejbArchive.toString(true));

        final EnterpriseArchive earArchive = ShrinkWrap.create(EnterpriseArchive.class)
            .addAsModule(webArchive).addAsModule(ejbArchive)
            .addAsManifestResource(ProfileInfoBean.class.getPackage(), "sun-application.xml", "sun-application.xml")
            .setApplicationXML(ProfileInfoBean.class.getPackage(), "application.xml");
        LOG.log(INFO, earArchive.toString(true));

        earFile = new File(tempDir, APP_NAME + ".ear");
        earArchive.as(ZipExporter.class).exportTo(earFile, true);
        assertThat(ASADMIN.exec("deploy", "--target", "server", earFile.getAbsolutePath()), asadminOK());

        baseUrl = new URL("http://localhost:8080/jsp2sful");
    }


    @AfterAll
    public static void cleanup() {
        ASADMIN.exec("undeploy", APP_NAME);
        ASADMIN.exec("delete-file-user", "--authrealmname", FILE_REALM_NAME, "--target", "server", USER_SHINGWAI_NAME);
        ASADMIN.exec("delete-file-user", "--authrealmname", FILE_REALM_NAME, "--target", "server", USER_SWCHAN_NAME);
        ASADMIN.exec("delete-file-user", "--authrealmname", FILE_REALM_NAME, "--target", "server", USER_WEBUSER_NAME);
        ASADMIN.exec("delete-file-user", "--authrealmname", FILE_REALM_NAME, "--target", "server", USER_NOAUTHUSER_NAME);
        delete(earFile);
    }


    @Test
    void shingwai() throws Exception {
        FormAuthHttpClient client = new FormAuthHttpClient(baseUrl, USER_SHINGWAI_NAME, USER_SHINGWAI_PASSWORD);
        HttpURLConnection connection = client.get("index.jsp");
        try {
            assertThat(connection.getResponseCode(), equalTo(200));
            final String text = readResponseBody(connection);
            assertThat(text, stringContainsInOrder(
                "JSP Page Access Profile",
                "The web user principal = shingwai",
                "Calling the ProfileInfoBean",
                "Looked up home!!",
                "Narrowed home!!",
                "Got the EJB!!",
                "User profile:",
                "shingwai",
                "Secret info:",
                "Keep It Secret!"
            ));
        } finally {
            connection.disconnect();
        }
    }


    @Test
    void swchan() throws Exception {
        FormAuthHttpClient client = new FormAuthHttpClient(baseUrl, USER_SWCHAN_NAME, USER_SWCHAN_PASSWORD);
        HttpURLConnection connection = client.get("index.jsp");
        try {
            assertThat(connection.getResponseCode(), equalTo(200));
            final String text = readResponseBody(connection);
            assertThat(text, stringContainsInOrder(
                "JSP Page Access Profile",
                "The web user principal = swchan",
                "Calling the ProfileInfoBean",
                "Looked up home!!",
                "Narrowed home!!",
                "Got the EJB!!",
                "User profile:",
                "swchan",
                "Secret info:",
                "CANNOT ACCESS getSecretInfo()"
            ));
        } finally {
            connection.disconnect();
        }
    }


    @Test
    void webuser() throws Exception {
        FormAuthHttpClient client = new FormAuthHttpClient(baseUrl, USER_WEBUSER_NAME, USER_WEBUSER_PASSWORD);
        HttpURLConnection connection = client.get("index.jsp");
        try {
            assertThat(connection.getResponseCode(), equalTo(200));
            final String text = readResponseBody(connection);
            assertThat(text, stringContainsInOrder(
                "JSP Page Access Profile",
                "The web user principal = webuser",
                "Calling the ProfileInfoBean",
                "Looked up home!!",
                "Narrowed home!!",
                "Got the EJB!!",
                "User profile:",
                "CANNOT ACCESS getCallerInfo()",
                "Secret info:",
                "CANNOT ACCESS getSecretInfo()"
            ));
        } finally {
            connection.disconnect();
        }
    }


    @Test
    void unauthorizedUser() throws Exception {
        FormAuthHttpClient client = new FormAuthHttpClient(baseUrl, USER_NOAUTHUSER_NAME, USER_NOAUTHUSER_PASSWORD);
        HttpURLConnection connection = client.get("index.jsp");
        try {
            assertThat(connection.getResponseCode(), equalTo(403));
        } finally {
            connection.disconnect();
        }
    }


    @Test
    void nosuchser() throws Exception {
        FormAuthHttpClient client = new FormAuthHttpClient(baseUrl, "idontexist", "whatever");
        HttpURLConnection connection = client.get("index.jsp");
        try {
            assertThat(connection.getResponseCode(), equalTo(200));
            final String text = readResponseBody(connection);
            assertThat(text, stringContainsInOrder(
                "A typical Error Page",
                "You could not be authenticated with the information provided.",
                "Please check your Username and Password."
            ));
        } finally {
            connection.disconnect();
        }
    }


    private static void delete(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }
}
