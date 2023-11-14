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

package org.glassfish.main.test.app.security.multirolemapping;

import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.net.HttpURLConnection;
import java.util.Base64;

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
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.glassfish.main.itest.tools.FormAuthHttpClient.readResponseBody;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.createFileUser;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.openConnection;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;


public class MultiRoleMappingTest {

    private static final Logger LOG = System.getLogger(MultiRoleMappingTest.class.getName());

    private static final String APP_NAME = "security-multirolemapping";
    private static final String FILE_REALM_NAME = "file";

    private static final String GROUP_ABC = "abc";
    private static final String GROUP_R1G1 = "r1g1";
    private static final String GROUP_R2G1 = "r2g1";
    private static final String GROUP_R2G2 = "r2g2";
    private static final String GROUP_R2G3 = "r2g3";
    private static final String GROUP_R3G1 = "r3g1";
    private static final String GROUP_R4G1 = "r4g1";
    private static final String GROUP_R5G1 = "r5g1";
    private static final String GROUP_R6G1 = "r6g1";

    private static final String USER_PASSWORD = "jakartaee";

    private static final String USER_R1P1_NAME = "r1p1";
    private static final String USER_R1P2_NAME = "r1p2";
    private static final String USER_R1P3_NAME = "r1p3";
    private static final String USER_R1G1USER_NAME = "r1g1user";

    private static final String USER_R2P1_NAME = "r2p1";
    private static final String USER_R2P2_NAME = "r2p2";
    private static final String USER_R2G1USER_NAME = "r2g1user";
    private static final String USER_R2G2USER_NAME = "r2g2user";
    private static final String USER_R2G3USER_NAME = "r2g3user";

    private static final String USER_R3P1_NAME = "r3p1";
    private static final String USER_R3P2_NAME = "r3p2";
    private static final String USER_R3G1USER_NAME = "r3g1user";

    private static final String USER_R4P1_NAME = "r4p1";
    private static final String USER_R4G1USER_NAME = "r4g1user";

    private static final String USER_R5P1_NAME = "r5p1";
    private static final String USER_R5P2_NAME = "r5p2";
    private static final String USER_R5G1USER_NAME = "r5g1user";

    private static final String USER_R6P1_NAME = "r6p1";
    private static final String USER_R6P2_NAME = "r6p2";
    private static final String USER_R6G1USER_NAME = "r6g1user";

    private static final String USER_R7P1_NAME = "r7p1";
    private static final String USER_R7P2_NAME = "r7p2";

    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();

    @TempDir
    private static File tempDir;
    private static File earFile;


    @BeforeAll
    public static void prepareDeployment() throws Exception {
        createFileUser(FILE_REALM_NAME, USER_R1P1_NAME, USER_PASSWORD, GROUP_ABC);
        createFileUser(FILE_REALM_NAME, USER_R1P2_NAME, USER_PASSWORD, GROUP_ABC);
        createFileUser(FILE_REALM_NAME, USER_R1P3_NAME, USER_PASSWORD, GROUP_ABC);
        createFileUser(FILE_REALM_NAME, USER_R1G1USER_NAME, USER_PASSWORD, GROUP_R1G1);
        createFileUser(FILE_REALM_NAME, USER_R2P1_NAME, USER_PASSWORD, GROUP_ABC);
        createFileUser(FILE_REALM_NAME, USER_R2P2_NAME, USER_PASSWORD, GROUP_ABC);
        createFileUser(FILE_REALM_NAME, USER_R2G1USER_NAME, USER_PASSWORD, GROUP_R2G1);
        createFileUser(FILE_REALM_NAME, USER_R2G2USER_NAME, USER_PASSWORD, GROUP_R2G2);
        createFileUser(FILE_REALM_NAME, USER_R2G3USER_NAME, USER_PASSWORD, GROUP_R2G3);
        createFileUser(FILE_REALM_NAME, USER_R3P1_NAME, USER_PASSWORD, GROUP_ABC);
        createFileUser(FILE_REALM_NAME, USER_R3P2_NAME, USER_PASSWORD, GROUP_ABC);
        createFileUser(FILE_REALM_NAME, USER_R3G1USER_NAME, USER_PASSWORD, GROUP_R3G1);
        createFileUser(FILE_REALM_NAME, USER_R4P1_NAME, USER_PASSWORD, GROUP_ABC);
        createFileUser(FILE_REALM_NAME, USER_R4G1USER_NAME, USER_PASSWORD, GROUP_R4G1);
        createFileUser(FILE_REALM_NAME, USER_R5P1_NAME, USER_PASSWORD, GROUP_ABC);
        createFileUser(FILE_REALM_NAME, USER_R5P2_NAME, USER_PASSWORD, GROUP_ABC);
        createFileUser(FILE_REALM_NAME, USER_R5G1USER_NAME, USER_PASSWORD, GROUP_R5G1);
        createFileUser(FILE_REALM_NAME, USER_R6P1_NAME, USER_PASSWORD, GROUP_ABC);
        createFileUser(FILE_REALM_NAME, USER_R6P2_NAME, USER_PASSWORD, GROUP_ABC);
        createFileUser(FILE_REALM_NAME, USER_R6G1USER_NAME, USER_PASSWORD, GROUP_R6G1);
        createFileUser(FILE_REALM_NAME, USER_R7P1_NAME, USER_PASSWORD, GROUP_ABC);
        createFileUser(FILE_REALM_NAME, USER_R7P2_NAME, USER_PASSWORD, GROUP_ABC);

        final WebArchive webArchive = ShrinkWrap.create(WebArchive.class, APP_NAME + "-web.war")
            .addClass(WebTest.class)
            .addAsWebInfResource(WebTest.class.getPackage(), "web.xml", "web.xml")
            .addAsWebInfResource(WebTest.class.getPackage(), "sun-web.xml", "sun-web.xml");
        LOG.log(INFO, webArchive.toString(true));

        final JavaArchive ejbArchive1 = ShrinkWrap.create(JavaArchive.class, APP_NAME + "1-ejb.jar")
            .addClasses(MessageBean1.class, MessageLocal1.class)
            .addAsManifestResource(WebTest.class.getPackage(), "sun-ejb-jar1.xml", "sun-ejb-jar.xml");
        LOG.log(INFO, ejbArchive1.toString(true));

        final JavaArchive ejbArchive2 = ShrinkWrap.create(JavaArchive.class, APP_NAME + "2-ejb.jar")
            .addClasses(MessageBean2.class, MessageLocal2.class)
            .addAsManifestResource(WebTest.class.getPackage(), "sun-ejb-jar2.xml", "sun-ejb-jar.xml");
        LOG.log(INFO, ejbArchive2.toString(true));

        final EnterpriseArchive earArchive = ShrinkWrap.create(EnterpriseArchive.class)
            .addAsModule(webArchive).addAsModule(ejbArchive1).addAsModule(ejbArchive2)
            .addAsManifestResource(WebTest.class.getPackage(), "sun-application.xml", "sun-application.xml")
            .setApplicationXML(WebTest.class.getPackage(), "application.xml");
        LOG.log(INFO, earArchive.toString(true));

        earFile = new File(tempDir, APP_NAME + ".ear");
        earArchive.as(ZipExporter.class).exportTo(earFile, true);
        assertThat(ASADMIN.exec("deploy", "--target", "server", earFile.getAbsolutePath()), asadminOK());
    }


    @AfterAll
    public static void cleanup() {
        ASADMIN.exec("undeploy", APP_NAME);
        String[] users = new String[] {USER_R1P1_NAME, USER_R1P2_NAME, USER_R1P3_NAME, USER_R1G1USER_NAME,
            USER_R2P1_NAME, USER_R2P2_NAME, USER_R2G1USER_NAME, USER_R2G2USER_NAME, USER_R2G3USER_NAME, USER_R3P1_NAME,
            USER_R3P2_NAME, USER_R3G1USER_NAME, USER_R4P1_NAME, USER_R4G1USER_NAME, USER_R5P1_NAME, USER_R5P2_NAME,
            USER_R5G1USER_NAME, USER_R6P1_NAME, USER_R6P2_NAME, USER_R6G1USER_NAME, USER_R7P1_NAME, USER_R7P2_NAME};
        for (String user : users) {
            ASADMIN.exec("delete-file-user", "--authrealmname", FILE_REALM_NAME, "--target", "server", user);
        }
        delete(earFile);
    }


    @Test
    void role1() throws Exception {
        check200("role1", USER_R1P1_NAME);
        check200("role1", USER_R1P2_NAME);
        check403("role1", USER_R1P3_NAME);
        check403("role1", USER_R2P1_NAME);
        check403("role1", USER_R1G1USER_NAME);
    }


    @Test
    void role2() throws Exception {
        check200("role2", USER_R2P1_NAME);
        check200("role2", USER_R2G1USER_NAME);
        check200("role2", USER_R2G2USER_NAME);
        check403("role2", USER_R2P2_NAME);
        check403("role2", USER_R2G3USER_NAME);
        check403("role2", USER_R1P1_NAME);
    }


    @Test
    void role3() throws Exception {
        check200("role3", USER_R3P1_NAME);
        check200("role3", USER_R3G1USER_NAME);
        check403("role3", USER_R3P2_NAME);
    }


    @Test
    void role4() throws Exception {
        check200("role4", USER_R4P1_NAME);
        check200("role4", USER_R4G1USER_NAME);
    }


    @Test
    void role5() throws Exception {
        check403("role5", USER_R5P1_NAME);
        check403("role5", USER_R5P2_NAME);
        check403("role5", USER_R5G1USER_NAME);
    }


    @Test
    void role6() throws Exception {
        check403("role6", USER_R6P1_NAME);
        check403("role6", USER_R6P2_NAME);
        check403("role6", USER_R6G1USER_NAME);
    }


    @Test
    void role7() throws Exception {
        check200("role7", USER_R7P1_NAME);
        check200("role7", USER_R7P2_NAME);
    }



    private void check200(String relativePath, String user) throws IOException {
        HttpURLConnection connection = prepareConnection(relativePath, user);
        try {
            assertThat(connection.getResponseCode(), equalTo(200));
            final String text = readResponseBody(connection);
            assertThat(text, equalTo("Hello " + relativePath + "\n"));
        } finally {
            connection.disconnect();
        }
    }


    private void check403(String relativePath, String user) throws IOException {
        HttpURLConnection connection = prepareConnection(relativePath, user);
        try {
            assertThat(connection.getResponseCode(), equalTo(403));
        } finally {
            connection.disconnect();
        }
    }


    private HttpURLConnection prepareConnection(String relativePath, String user) throws IOException {
        HttpURLConnection connection = openConnection(8080, "/" + APP_NAME + "/" + relativePath);
        connection.setRequestMethod("GET");
        String basicAuth = Base64.getEncoder().encodeToString((user + ":" + USER_PASSWORD).getBytes(UTF_8));
        connection.setRequestProperty("Authorization", "Basic " + basicAuth);
        return connection;
    }


    private static void delete(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }
}
