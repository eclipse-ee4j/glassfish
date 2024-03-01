/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.test.app.security.jmac.http.servlet.basic;

import java.io.File;
import java.io.InputStream;
import java.lang.System.Logger;
import java.net.HttpURLConnection;
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


public class HttpServletBasicAuthTest {

    private static final Logger LOG = System.getLogger(HttpServletBasicAuthTest.class.getName());

    private static final String APP_NAME = "security-jmac-httpservlet";
    private static final String AUTH_MODULE_NAME = "httpServletTestAuthModule";
    private static final String FILE_REALM_NAME = "file123";
    private static final String USER_NAME = "shingwai123";
    private static final String USER_PASSWORD = "password123";

    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();

    @TempDir
    private static File tempDir;
    private static File warFile;
    private static File keyFile;

    private static File loginModuleFile;

    @BeforeAll
    public static void prepareDeployment() {
        keyFile = getDomain1Directory().resolve(Path.of("config", "keyfile123.txt")).toFile();
        assertThat(ASADMIN.exec("create-auth-realm",
            "--classname", "com.sun.enterprise.security.auth.realm.file.FileRealm",
            "--property", "file=" + keyFile.getAbsolutePath().replaceAll("([\\\\:])", "\\\\$1") + ":jaas-context=fileRealm",
            "--target", "server", FILE_REALM_NAME),
            asadminOK());
        createFileUser(FILE_REALM_NAME, USER_NAME, USER_PASSWORD, "mygroup");

        JavaArchive loginModule = ShrinkWrap.create(JavaArchive.class).addClass(MyHttpServletResponseWrapper.class)
            .addClass(HttpServletTestAuthModule.class).addClass(MyPrintWriter.class);
        LOG.log(INFO, loginModule.toString(true));
        loginModuleFile = new File(getDomain1Directory().toAbsolutePath().resolve("../../lib").toFile(),
            "testLoginModule.jar");
        loginModule.as(ZipExporter.class).exportTo(loginModuleFile, true);

        assertThat(ASADMIN.exec("create-message-security-provider",
            "--classname", HttpServletTestAuthModule.class.getName(),
            "--layer", "HttpServlet", "--providertype", "server", "--requestauthsource", "sender",
            AUTH_MODULE_NAME), asadminOK());

        WebArchive webArchive = ShrinkWrap.create(WebArchive.class)
            .addAsWebResource(HttpServletBasicAuthTest.class.getPackage(), "index.jsp", "index.jsp")
            .addAsWebInfResource(HttpServletBasicAuthTest.class.getPackage(), "web.xml", "web.xml")
            .addAsWebInfResource(HttpServletBasicAuthTest.class.getPackage(), "glassfish-web.xml", "glassfish-web.xml");

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
        ASADMIN.exec("delete-auth-realm", FILE_REALM_NAME);
        delete(warFile);
        delete(keyFile);
        delete(loginModuleFile);
    }


    @Test
    void test() throws Exception {
        HttpURLConnection connection = openConnection(8080, "/" + APP_NAME + "/index.jsp");
        connection.setRequestMethod("GET");
        String basicAuth = Base64.getEncoder().encodeToString((USER_NAME + ":" + USER_PASSWORD).getBytes(UTF_8));
        connection.setRequestProperty("Authorization", "Basic " + basicAuth);
        assertThat(connection.getResponseCode(), equalTo(200));
        try (InputStream is = connection.getInputStream()) {
            String text = new String(is.readAllBytes(), UTF_8);
            assertThat(text, stringContainsInOrder(
                "Hello World from 196 HttpServlet AuthModule Test!",
                "Hello, shingwai123 from " + HttpServletTestAuthModule.class.getName(),
                "PC = security-jmac-httpservlet/security-jmac-httpservlet",
                "Adjusted count: 230"));
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
