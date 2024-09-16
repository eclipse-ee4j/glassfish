/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.test.app.applib;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.text.MessageFormat;

import org.glassfish.common.util.HttpParser;
import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.itest.tools.asadmin.AsadminResult;
import org.glassfish.main.test.app.applib.lib.LibraryResource;
import org.glassfish.main.test.app.applib.webapp.LibraryApplication;
import org.glassfish.main.test.app.applib.webapp.LibraryEndpoint;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.io.TempDir;

import static java.lang.System.Logger.Level.INFO;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.openConnection;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@TestMethodOrder(OrderAnnotation.class)
public class ApplicationLibraryTest {

    private static final System.Logger LOG = System.getLogger(ApplicationLibraryTest.class.getName());

    private static final String VERSION_RESOURCE = "src/main/resources/org/glassfish/main/test/app/applib/v{0}/Version.properties";

    private static final String APPLIB_FILE_NAME = "applib.jar";

    private static final String WEBAPP_FILE_NAME = "webapp.war";

    private static final String WEBAPP1_NAME = "webapp1";

    private static final String WEBAPP2_NAME = "webapp2";

    private static final String WEBAPP3_NAME = "webapp3";

    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();

    @TempDir
    private static File appLibDir;

    @TempDir
    private static File webAppDir;

    @BeforeAll
    public static void deployAll() throws IOException {
        File webApp = createWebApp();

        File appLib = createAppLib(1);

        // The 'webapp1' and 'webapp2' uses the same version
        // of the application library, whereas 'webapp3' uses
        // its own new version.

        AsadminResult result = ASADMIN.exec("deploy",
            "--contextroot", "/" + WEBAPP1_NAME,
            "--name", WEBAPP1_NAME,
            "--libraries", appLib.getAbsolutePath(),
            webApp.getAbsolutePath());
        assertThat(result, asadminOK());

        result = ASADMIN.exec("deploy",
            "--contextroot", "/" + WEBAPP2_NAME,
            "--name", WEBAPP2_NAME,
            "--libraries", appLib.getAbsolutePath(),
            webApp.getAbsolutePath());
        assertThat(result, asadminOK());

        // Remove old version of the application library
        Files.deleteIfExists(appLib.toPath());

        // And create the new one
        appLib = createAppLib(2);

        result = ASADMIN.exec("deploy",
            "--contextroot", "/" + WEBAPP3_NAME,
            "--name", WEBAPP3_NAME,
            "--libraries", appLib.getAbsolutePath(),
            webApp.getAbsolutePath());
        assertThat(result, asadminOK());
    }

    @AfterAll
    public static void undeployAll() {
        assertAll(
            () -> assertThat(ASADMIN.exec("undeploy", WEBAPP1_NAME), asadminOK()),
            () -> assertThat(ASADMIN.exec("undeploy", WEBAPP2_NAME), asadminOK()),
            () -> assertThat(ASADMIN.exec("undeploy", WEBAPP3_NAME), asadminOK())
        );
    }

    @Test
    @Order(1)
    public void testFindClass() throws IOException {
        String version1 = getEntity(WEBAPP1_NAME, "version");
        String version2 = getEntity(WEBAPP2_NAME, "version");
        String version3 = getEntity(WEBAPP3_NAME, "version");

        assertAll(
            () -> assertThat(Integer.parseInt(version1), equalTo(1)),
            () -> assertThat(Integer.parseInt(version2), equalTo(1)),
            () -> assertThat(Integer.parseInt(version3), equalTo(2))
        );
    }

    @Test
    @Order(2)
    public void testFindResource() throws IOException {
        String version1 = getEntity(WEBAPP1_NAME, "resource");
        String version2 = getEntity(WEBAPP2_NAME, "resource");
        String version3 = getEntity(WEBAPP3_NAME, "resource");

        assertAll(
            () -> assertThat(version1, equalTo("{version=1}")),
            () -> assertThat(version2, equalTo("{version=1}")),
            () -> assertThat(version3, equalTo("{version=2}"))
        );
    }

    @Test
    @Order(3)
    public void testApplicationLibrarySharing() throws IOException {
        String uuid1 = getEntity(WEBAPP1_NAME, "uuid");
        String uuid2 = getEntity(WEBAPP2_NAME, "uuid");
        String uuid3 = getEntity(WEBAPP3_NAME, "uuid");

        assertAll(
            // Both 'webapp1' and 'webapp2' share the same library
            () -> assertEquals(uuid1, uuid2),
            // The 'webapp3' uses its own newer version
            () -> assertNotEquals(uuid1, uuid3)
        );
    }

    @Test
    @Order(99)
    public void testApplicationLibrariesPreserveBetweenRestarts() throws IOException {
        assertThat(ASADMIN.exec("stop-domain"), asadminOK());
        assertThat(ASADMIN.exec("start-domain"), asadminOK());

        String version1 = getEntity(WEBAPP1_NAME, "version");
        String version2 = getEntity(WEBAPP2_NAME, "version");
        String version3 = getEntity(WEBAPP3_NAME, "version");

        // Should be used latest library version
        assertAll(
            () -> assertThat(Integer.parseInt(version1), equalTo(2)),
            () -> assertThat(Integer.parseInt(version2), equalTo(2)),
            () -> assertThat(Integer.parseInt(version3), equalTo(2))
        );

        String uuid1 = getEntity(WEBAPP1_NAME, "uuid");
        String uuid2 = getEntity(WEBAPP2_NAME, "uuid");
        String uuid3 = getEntity(WEBAPP3_NAME, "uuid");

        // The same application library should be shared by all three apps
        assertThat(uuid1, allOf(equalTo(uuid2), equalTo(uuid3)));
    }

    private String getEntity(String contextRoot, String endpoint) throws IOException {
        HttpURLConnection connection = openConnection(8080, "/" + contextRoot + "/" + endpoint);
        connection.setRequestMethod("GET");
        try {
            assertThat(connection.getResponseCode(), equalTo(200));
            return HttpParser.readResponseInputStream(connection);
        } finally {
            connection.disconnect();
        }
    }

    private static File createAppLib(int version) {
        JavaArchive javaArchive = ShrinkWrap.create(JavaArchive.class)
            .addClasses(LibraryResource.class)
            .addAsResource(
                new File(MessageFormat.format(VERSION_RESOURCE, version)),
                "/org/glassfish/main/test/app/applib/Version.properties"
            );

        LOG.log(INFO, javaArchive.toString(true));

        File appLib = new File(appLibDir, APPLIB_FILE_NAME);
        javaArchive.as(ZipExporter.class).exportTo(appLib, true);
        return appLib;
    }

    private static File createWebApp() {
        WebArchive webArchive = ShrinkWrap.create(WebArchive.class)
            .addClass(LibraryEndpoint.class)
            .addClass(LibraryApplication.class);

        LOG.log(INFO, webArchive.toString(true));

        File webApp = new File(webAppDir, WEBAPP_FILE_NAME);
        webArchive.as(ZipExporter.class).exportTo(webApp, true);
        return webApp;
    }
}
