/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.test.app.mpconfig;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;

import org.glassfish.common.util.HttpParser;
import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.test.app.mpconfig.lib.ConfigHolder;
import org.glassfish.main.test.app.mpconfig.webapp.ConfigEndpoint;
import org.glassfish.main.test.app.mpconfig.webapp.ConfigApplication;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
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
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@TestMethodOrder(OrderAnnotation.class)
public class ApplicationScopedMpConfigTest {

    private static final System.Logger LOG = System.getLogger(ApplicationScopedMpConfigTest.class.getName());

    private static final String SHARED_LIBRARY_NAME = "mpconfig-lib";

    private static final String APP_NAME = "mpconfig-app";

    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();

    @TempDir
    private static File tempDir;

    private static File libraryJar;

    @BeforeAll
    public static void setup() throws IOException {
        libraryJar = createLibrary();
        assertThat(ASADMIN.exec("add-library", libraryJar.getAbsolutePath()), asadminOK());
    }

    @AfterAll
    public static void cleanup() {
        assertThat(ASADMIN.exec("remove-library", libraryJar.getName()), asadminOK());
    }

    @Test
    @Order(1)
    public void testApp1Config() throws IOException {
        File app1 = createWebApp("app1");
        final String appName = APP_NAME + "-app1";
        assertThat(ASADMIN.exec("deploy", "--force", "--contextroot", "app", "--name", appName, app1.getAbsolutePath()), asadminOK());

        String configValue = getConfigName();
        assertThat(configValue, equalTo("app1"));

        assertThat(ASADMIN.exec("undeploy", appName), asadminOK());
    }

    @Test
    @Order(2)
    public void testApp2Config() throws IOException {
        File app2 = createWebApp("app2");
        final String appName = APP_NAME + "-app2";
        assertThat(ASADMIN.exec("deploy", "--force", "--contextroot", "app", "--name", appName, app2.getAbsolutePath()), asadminOK());

        String configValue = getConfigName();
        assertThat(configValue, equalTo("app2"));

        assertThat(ASADMIN.exec("undeploy", appName), asadminOK());
    }

    private String getConfigName() throws IOException {
        HttpURLConnection connection = openConnection(8080, "/app/config/name");
        connection.setRequestMethod("GET");
        try {
            assertThat(connection.getResponseCode(), equalTo(200));
            return HttpParser.readResponseInputStream(connection);
        } finally {
            connection.disconnect();
        }
    }

    // create a shared library with Config stored in a static field, so that it's shared across applications
    private static File createLibrary() {
        JavaArchive library = ShrinkWrap.create(JavaArchive.class, SHARED_LIBRARY_NAME + ".jar")
            .addClass(ConfigHolder.class);

        LOG.log(INFO, library.toString(true));

        File libraryFile = new File(tempDir, SHARED_LIBRARY_NAME + ".jar");
        library.as(ZipExporter.class).exportTo(libraryFile, true);
        return libraryFile;
    }

    private static File createWebApp(String configValue) {
        String configProperties = "name=" + configValue;

        WebArchive webApp = ShrinkWrap.create(WebArchive.class)
            .addClass(ConfigEndpoint.class)
            .addClass(ConfigApplication.class)
            .addAsResource(new StringAsset(configProperties), "META-INF/microprofile-config.properties");

        LOG.log(INFO, webApp.toString(true));

        File webAppFile = new File(tempDir, "app-" + configValue + ".war");
        webApp.as(ZipExporter.class).exportTo(webAppFile, true);
        return webAppFile;
    }
}
