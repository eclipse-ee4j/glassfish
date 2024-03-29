/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package org.glassfish.main.test.app.security.jmac.http.servlet.form;

import java.io.File;
import java.lang.System.Logger;
import java.net.HttpURLConnection;
import java.net.URL;

import org.glassfish.main.itest.tools.FormAuthHttpClient;
import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static java.lang.System.Logger.Level.INFO;
import static org.glassfish.main.itest.tools.FormAuthHttpClient.readResponseBody;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.createFileUser;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.getDomain1Directory;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;


public class HttpServletFormAuthTest {

    private static final Logger LOG = System.getLogger(HttpServletFormAuthTest.class.getName());

    private static final String APP_NAME = "security-jmac-http-servlet-form";
    private static final String AUTH_MODULE_NAME = "httpServletFormTestAuthModule";
    private static final String FILE_REALM_NAME = "file";
    private static final String USER_NAME = "shingwai";
    private static final String USER_PASSWORD = "shingwai";

    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();

    @TempDir
    private static File tempDir;
    private static File warFile;

    private static File loginModuleFile;

    @BeforeAll
    public static void prepareDeployment() {
        createFileUser(FILE_REALM_NAME, USER_NAME, USER_PASSWORD, "mygroup");

        final JavaArchive loginModule = ShrinkWrap.create(JavaArchive.class).addClass(HttpServletFormTestAuthModule.class)
            .addClass(SavedRequest.class);
        LOG.log(INFO, loginModule.toString(true));
        loginModuleFile = new File(getDomain1Directory().toAbsolutePath().resolve("../../lib").toFile(),
            "testFormLoginModule.jar");
        loginModule.as(ZipExporter.class).exportTo(loginModuleFile, true);

        assertThat(ASADMIN.exec("create-message-security-provider",
            "--classname", HttpServletFormTestAuthModule.class.getName(),
            "--layer", "HttpServlet", "--providertype", "server", "--requestauthsource", "sender",
            AUTH_MODULE_NAME), asadminOK());

        final WebArchive webArchive = ShrinkWrap.create(WebArchive.class)
            .addAsWebResource(HttpServletFormAuthTest.class.getPackage(), "error.html", "error.html")
            .addAsWebResource(HttpServletFormAuthTest.class.getPackage(), "index.jsp", "index.jsp")
            .addAsWebResource(HttpServletFormAuthTest.class.getPackage(), "login.jsp", "login.jsp")
            .addAsWebInfResource(HttpServletFormAuthTest.class.getPackage(), "web.xml", "web.xml")
            .addAsWebInfResource(HttpServletFormAuthTest.class.getPackage(), "sun-web.xml", "sun-web.xml");

        LOG.log(INFO, webArchive.toString(true));

        warFile = new File(tempDir, APP_NAME + ".war");
        webArchive.as(ZipExporter.class).exportTo(warFile, true);
        assertThat(ASADMIN.exec("deploy", "--libraries", loginModuleFile.getAbsolutePath(), "--target", "server",
            warFile.getAbsolutePath()), asadminOK());
    }


    @AfterAll
    public static void cleanup() {
        ASADMIN.exec("undeploy", APP_NAME);
        ASADMIN.exec("delete-file-user", "--authrealmname", FILE_REALM_NAME, "--target", "server", USER_NAME);
        ASADMIN.exec("delete-message-security-provider", "--layer", "HttpServlet", AUTH_MODULE_NAME);
        delete(warFile);
        delete(loginModuleFile);
    }


    @Test
    void test() throws Exception {
        final URL baseUrl = new URL("http://localhost:8080/" + APP_NAME);
        final FormAuthHttpClient client = new FormAuthHttpClient(baseUrl, USER_NAME, USER_PASSWORD);
        final HttpURLConnection connection = client.get("index.jsp");
        try {
            assertThat(connection.getResponseCode(), equalTo(200));
            final String text = readResponseBody(connection);
            assertThat(text, stringContainsInOrder(
                "Hello World from 196 HttpServletForm AuthModule Test!",
                "Hello, shingwai from " + HttpServletFormTestAuthModule.class.getName(),
                "PC = security-jmac-http-servlet-form/security-jmac-http-servlet-form"));
        } finally {
            connection.disconnect();
        }
    }

    private static void delete(final File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }
}
