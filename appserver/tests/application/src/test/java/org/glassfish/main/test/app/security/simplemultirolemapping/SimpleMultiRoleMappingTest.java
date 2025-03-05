/*
 * Copyright (c) 2023,2025 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.test.app.security.simplemultirolemapping;

import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.net.HttpURLConnection;
import java.util.Base64;

import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.TestUtilities;
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
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.glassfish.main.itest.tools.FormAuthHttpClient.readResponseBody;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.createFileUser;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.openConnection;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;


public class SimpleMultiRoleMappingTest {

    private static final Logger LOG = System.getLogger(SimpleMultiRoleMappingTest.class.getName());

    private static final String APP_NAME = "security-multimapping";
    private static final String FILE_REALM_NAME = "file";

    private static final String USER_WEBUSER_NAME = "webuser";
    private static final String USER_WEBUSER_PASSWORD = "ww";
    private static final String USER_BOBBY_NAME = "bobby";
    private static final String USER_BOBBY_PASSWORD = "bb";

    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();

    @TempDir
    private static File tempDir;
    private static File earFile;

    @BeforeAll
    public static void prepareDeployment() throws Exception {
        createFileUser(FILE_REALM_NAME, USER_WEBUSER_NAME, USER_WEBUSER_PASSWORD, "webusers");
        createFileUser(FILE_REALM_NAME, USER_BOBBY_NAME, USER_BOBBY_PASSWORD, "dummygroup");

        final WebArchive webArchive = ShrinkWrap.create(WebArchive.class, APP_NAME + "-web.war")
            .addClasses(EjbTest.class, WebTest.class)
            .addAsWebInfResource(WebTest.class.getPackage(), "web.xml", "web.xml")
            .addAsWebInfResource(WebTest.class.getPackage(), "sun-web.xml", "sun-web.xml");
        LOG.log(INFO, webArchive.toString(true));

        final JavaArchive ejbArchive = ShrinkWrap.create(JavaArchive.class, APP_NAME + "-ejb.jar")
            .addClasses(MessageBean.class, MessageLocal.class)
            .addAsManifestResource(WebTest.class.getPackage(), "sun-ejb-jar.xml", "sun-ejb-jar.xml");
        LOG.log(INFO, ejbArchive.toString(true));

        final EnterpriseArchive earArchive = ShrinkWrap.create(EnterpriseArchive.class)
            .addAsModule(webArchive).addAsModule(ejbArchive)
            .setApplicationXML(WebTest.class.getPackage(), "application.xml");
        LOG.log(INFO, earArchive.toString(true));

        earFile = new File(tempDir, APP_NAME + ".ear");
        earArchive.as(ZipExporter.class).exportTo(earFile, true);
        assertThat(ASADMIN.exec("deploy", "--target", "server", earFile.getAbsolutePath()), asadminOK());
    }

    @AfterAll
    public static void cleanup() throws Exception {
        ASADMIN.exec("undeploy", APP_NAME);
        ASADMIN.exec("delete-file-user", "--authrealmname", FILE_REALM_NAME, "--target", "server", USER_WEBUSER_NAME);
        ASADMIN.exec("delete-file-user", "--authrealmname", FILE_REALM_NAME, "--target", "server", USER_BOBBY_NAME);
        TestUtilities.delete(earFile);
    }

    @Test
    void ejb() throws Exception {
        String bobby = getContent("ejb", USER_BOBBY_NAME, USER_BOBBY_PASSWORD);
        assertThat(bobby, stringContainsInOrder("Servlet EjbTest", "Hello from ejb"));
    }

    @Test
    void web() throws Exception {
        String webtest = getContent("web", USER_WEBUSER_NAME, USER_WEBUSER_PASSWORD);
        assertThat(webtest, stringContainsInOrder("Servlet WebTest", "<h2>Ok</h2>"));
    }

    private String getContent(String relativePath, String user, String password) throws IOException {
        HttpURLConnection connection = prepareConnection(relativePath, user, password);
        try {
            assertThat(connection.getResponseCode(), equalTo(200));
            return readResponseBody(connection);
        } finally {
            connection.disconnect();
        }
    }

    private HttpURLConnection prepareConnection(String relativePath, String user, String password) throws IOException {
        HttpURLConnection connection = openConnection(8080, "/" + APP_NAME + "/" + relativePath);
        connection.setRequestMethod("GET");
        String basicAuth = Base64.getEncoder().encodeToString((user + ":" + password).getBytes(UTF_8));
        connection.setRequestProperty("Authorization", "Basic " + basicAuth);
        return connection;
    }
}
