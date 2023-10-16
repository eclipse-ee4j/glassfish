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

package org.glassfish.main.test.app.security.jmac.http.servlet.challenge;

import java.io.File;
import java.io.InputStream;
import java.lang.System.Logger;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.util.Base64;

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
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.createFileUser;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.getDomain1Directory;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.openConnection;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;


public class HttpServletChallengeAuthTest {
    private static final Logger LOG = System.getLogger(HttpServletChallengeAuthTest.class.getName());

    private static final String APP_NAME = "security-jmac-http-servlet-challenge";
    private static final String AUTH_MODULE_NAME = "httpServletChallengeTestAuthModule";
    private static final String FILE_REALM_NAME = "file123";
    private static final String USER_NAME = "shingwai";
    private static final String USER_NAME2 = "shingwai_2";
    private static final String USER_PASSWORD = "shingwai";
    private static final String USER_PASSWORD2 = "adminadmin";

    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();

    @TempDir
    private static File tempDir;
    private static File warFile;
    private static File keyFile;

    private static File loginModuleFile;

    @BeforeAll
    public static void prepareDeployment() {
        keyFile = getDomain1Directory().resolve(Path.of("config", "file123.txt")).toFile();
        assertThat(ASADMIN.exec("create-auth-realm", "--classname",
            "com.sun.enterprise.security.auth.realm.file.FileRealm", "--property",
            "file=" + keyFile.getAbsolutePath() + ":jaas-context=fileRealm", "--target", "server", FILE_REALM_NAME),
            asadminOK());
        createFileUser(FILE_REALM_NAME, USER_NAME, USER_PASSWORD, "mygroup");
        createFileUser(FILE_REALM_NAME, USER_NAME2, USER_PASSWORD2, "mygroup");

        final JavaArchive loginModule = ShrinkWrap.create(JavaArchive.class)
            .addClass(HttpServletChallengeTestAuthModule.class);
        LOG.log(INFO, loginModule.toString(true));
        // FIXME: When you use the same name as in HttpServletBasicAuthTest, the test will fail with HTTP500.
        loginModuleFile = new File(getDomain1Directory().toAbsolutePath().resolve("../../lib").toFile(),
            "testLoginModuleChallenge.jar");
        loginModule.as(ZipExporter.class).exportTo(loginModuleFile, true);

        assertThat(ASADMIN.exec("create-message-security-provider",
            "--classname", HttpServletChallengeTestAuthModule.class.getName(),
            "--layer", "HttpServlet", "--providertype", "server", "--requestauthsource", "sender",
            AUTH_MODULE_NAME), asadminOK());

        final WebArchive webArchive = ShrinkWrap.create(WebArchive.class)
            .addAsWebResource(HttpServletChallengeAuthTest.class.getPackage(), "index.jsp", "index.jsp")
            .addAsWebInfResource(HttpServletChallengeAuthTest.class.getPackage(), "web.xml", "web.xml")
            .addAsWebInfResource(HttpServletChallengeAuthTest.class.getPackage(), "sun-web.xml", "sun-web.xml");

        LOG.log(INFO, webArchive.toString(true));

        warFile = new File(tempDir, APP_NAME + ".war");
        webArchive.as(ZipExporter.class).exportTo(warFile, true);
        assertThat(ASADMIN.exec("deploy", "--libraries", loginModuleFile.getAbsolutePath(), "--target", "server",
            warFile.getAbsolutePath()), asadminOK());
    }


    @AfterAll
    public static void cleanup() {
        ASADMIN.exec("undeploy", APP_NAME);
        ASADMIN.exec("delete-file-user", "--authrealmname", FILE_REALM_NAME, "--target", "server", USER_NAME2);
        ASADMIN.exec("delete-file-user", "--authrealmname", FILE_REALM_NAME, "--target", "server", USER_NAME);
        ASADMIN.exec("delete-message-security-provider", "--layer", "HttpServlet", AUTH_MODULE_NAME);
        ASADMIN.exec("delete-auth-realm", FILE_REALM_NAME);
        delete(warFile);
        delete(keyFile);
        delete(loginModuleFile);
    }


    @Test
    void test() throws Exception {
        final CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
        final HttpCookie sessionId;
        final HttpURLConnection connection = openConnection(8080, "/" + APP_NAME + "/index.jsp");
        try {
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            final String basicAuth = Base64.getEncoder().encodeToString((USER_NAME + ":" + USER_PASSWORD).getBytes(UTF_8));
            connection.setRequestProperty("Authorization", "Basic " + basicAuth);
            assertThat(connection.getResponseCode(), equalTo(401));
            sessionId = cookieManager.getCookieStore().getCookies().stream()
                .filter(c -> c.getName().equals("JSESSIONID")).findFirst().get();
        } finally {
            connection.disconnect();
        }
        final HttpURLConnection connection2 = openConnection(8080, "/" + APP_NAME + "/index.jsp");
        connection2.setRequestProperty("Cookie", "JSESSIONID=" + URLEncoder.encode(sessionId.getValue(), "UTF-8"));
        connection2.setRequestMethod("GET");
        final String basicAuth = Base64.getEncoder().encodeToString((USER_NAME + ":" + USER_PASSWORD2).getBytes(UTF_8));
        connection2.setRequestProperty("Authorization", "Basic " + basicAuth);
        assertThat(connection2.getResponseCode(), equalTo(200));
        try (InputStream is = connection2.getInputStream()) {
            final String text = new String(is.readAllBytes(), UTF_8);
            assertThat(text, stringContainsInOrder(
                "Hello World from 196 HttpServletChallenge AuthModule Test!",
                "Hello, shingwai from " + HttpServletChallengeTestAuthModule.class.getName(),
                "with authType MC"));
        } finally {
            connection2.disconnect();
        }
    }

    private static void delete(final File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }
}
