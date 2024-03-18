/*
 * Copyright (c) 2023, 2024 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.test.app.security.defaultp2r;

import static java.lang.System.Logger.Level.INFO;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.createFileUser;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.File;
import java.lang.System.Logger;
import java.net.URI;
import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.HttpClient10;
import org.glassfish.main.itest.tools.HttpClient10.HttpResponse;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.itest.tools.asadmin.AsadminResult;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class DefaultP2RAuthTest {

    private static final Logger LOG = System.getLogger(DefaultP2RAuthTest.class.getName());

    private static final String APP_NAME = "security-defaultP2R-servlet";
    private static final String FILE_REALM_NAME = "file";
    private static final String SERVER_CFG_PROPERTY = "server-config.security-service.activate-default-principal-to-role-mapping";

    private static final String USER_NAME = "bobby";
    private static final String USER_PASSWORD = "bb";

    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();

    @TempDir
    private static File tempDir;
    private static File warFile;

    private static String orignalCfgValue;


    @BeforeAll
    public static void prepareDeployment() {
        AsadminResult result = ASADMIN.exec("--terse", "get", SERVER_CFG_PROPERTY);
        orignalCfgValue = result.getStdOut().replaceFirst(SERVER_CFG_PROPERTY + "=", "").trim();
        createFileUser(FILE_REALM_NAME, USER_NAME, USER_PASSWORD, "mygroup");

        WebArchive webArchive = ShrinkWrap.create(WebArchive.class)
            .addClass(DefaultP2RTestServlet.class)
            .addAsWebInfResource(DefaultP2RTestServlet.class.getPackage(), "web.xml", "web.xml");

        LOG.log(INFO, webArchive.toString(true));

        warFile = new File(tempDir, APP_NAME + ".war");
        webArchive.as(ZipExporter.class).exportTo(warFile, true);
    }


    @AfterEach
    public void undeploy() {
        // The application must be undeployed and deployed again to accept the new server-config value
        ASADMIN.exec("undeploy", APP_NAME);
    }


    @AfterAll
    public static void cleanup() {
        assertThat(ASADMIN.exec("set", SERVER_CFG_PROPERTY + "=" + orignalCfgValue), asadminOK());
        ASADMIN.exec("delete-file-user", "--authrealmname", FILE_REALM_NAME, "--target", "server", USER_NAME);
        delete(warFile);
    }


    @Test
    void p2rEnabled() throws Exception {
        LOG.log(INFO, "Running p2rEnabled");

        assertThat(ASADMIN.exec("set", SERVER_CFG_PROPERTY + "=true"), asadminOK());
        assertThat(ASADMIN.exec("deploy", "--target", "server", warFile.getAbsolutePath()), asadminOK());

        HttpClient10 client = new HttpClient10(new URI("http://localhost:8080/" + APP_NAME + "/TestServlet").toURL(),
            USER_NAME, USER_PASSWORD);

        HttpResponse responseFoo = client.send("FOO", null);
        assertAll(
            () -> assertThat(responseFoo.responseLine, equalTo("HTTP/1.1 200 OK")),
            () -> assertThat(responseFoo.body, equalTo("doFoo with bobby"))
        );

        HttpResponse responseGet = client.send("GET", null);
        assertAll(
            () -> assertThat(responseGet.responseLine, equalTo("HTTP/1.1 200 OK")),
            () -> assertThat(responseGet.body, equalTo("doGet with bobby"))
        );
    }


    @Test
    void p2rDisabled() throws Exception {
        LOG.log(INFO, "Running p2rDisabled");

        assertThat(ASADMIN.exec("set", SERVER_CFG_PROPERTY + "=false"), asadminOK());
        assertThat(ASADMIN.exec("deploy", "--target", "server", warFile.getAbsolutePath()), asadminOK());

        HttpClient10 client = new HttpClient10(new URI("http://localhost:8080/" + APP_NAME + "/TestServlet").toURL(),
            USER_NAME, USER_PASSWORD);

        HttpResponse responseGet = client.send("GET", null);
        assertThat(responseGet.responseLine, equalTo("HTTP/1.1 403 Forbidden"));
    }

    private static void delete(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }
}
