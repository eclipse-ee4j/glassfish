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
package org.glassfish.main.test.app.connpool;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.itest.tools.asadmin.AsadminResult;
import org.glassfish.main.itest.tools.asadmin.DomainSettings;
import org.glassfish.main.test.app.connpool.lib.LastTraceSQLTraceListener;
import org.glassfish.main.test.app.connpool.webapp.Employee;
import org.glassfish.main.test.app.connpool.webapp.SqlListenerApplication;
import org.glassfish.main.test.app.connpool.webapp.SqlListenerEndpoint;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static java.lang.System.Logger.Level.INFO;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.openConnection;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class SQLTraceListenerTest {

    private static final System.Logger LOG = System.getLogger(SQLTraceListenerTest.class.getName());

    private static final String RESOURCE_ROOT = "src/main/resources/" + SqlListenerApplication.class.getPackageName().replace(".", "/");

    private static final String LIB_FILE_NAME = "lib.jar";

    private static final String WEBAPP_FILE_NAME = "webapp.war";

    private static final String WEBAPP_NAME = "webapp";

    private static final String POOL_NAME = "DerbyPool";

    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();

    private static final DomainSettings DOMAIN_SETTINGS = new DomainSettings(ASADMIN);

    @TempDir
    private static File appLibDir;

    @TempDir
    private static File webAppDir;

    @BeforeAll
    public static void deployAll() throws IOException {
        AsadminResult result;

        File webApp = createWebApp();

        File lib = createSqlTraceListenerLib();

        result = ASADMIN.exec("add-library", lib.getAbsolutePath());
        assertThat(result, asadminOK());

        // add-library requires restart
        result = ASADMIN.exec("restart-domain");
        assertThat(result, asadminOK());

        DOMAIN_SETTINGS.backupDerbyPoolSettings();
        DOMAIN_SETTINGS.setDerbyPoolEmbededded();

        result = ASADMIN.exec("set", "resources.jdbc-connection-pool." + POOL_NAME
                + ".sql-trace-listeners=" + LastTraceSQLTraceListener.class.getName());
        assertThat(result, asadminOK());

        result = ASADMIN.exec("deploy",
                "--contextroot", "/" + WEBAPP_NAME,
                "--name", WEBAPP_NAME,
                webApp.getAbsolutePath());
        assertThat(result, asadminOK());
    }

    @AfterAll
    public static void undeployAll() {
        DOMAIN_SETTINGS.restoreSettings();
        assertAll(
                () -> assertThat(ASADMIN.exec("undeploy", WEBAPP_NAME), asadminOK()),
                () -> assertThat(ASADMIN.exec("set", "resources.jdbc-connection-pool." + POOL_NAME
                        + ".sql-trace-listeners="), asadminOK()),
                () -> assertThat(ASADMIN.exec("remove-library", LIB_FILE_NAME), asadminOK())
        );
    }

    @Test
    public void testSQLQueryWithListener() throws IOException {
        createWebApp();
        assertValidTraceRecordReceived(WEBAPP_NAME, "validate-trace-listener");
    }

    private void assertValidTraceRecordReceived(String contextRoot, String endpoint) throws IOException {
        HttpURLConnection connection = openConnection(8080, "/" + contextRoot + "/" + endpoint);
        connection.setRequestMethod("GET");
        try {
            try {
                assertThat(connection.getResponseCode(), equalTo(200));
            } catch (AssertionError e) {
                throw new AssertionError(readErrorResponse(connection), e);
            }
        } finally {
            connection.disconnect();
        }
    }

    private String readErrorResponse(HttpURLConnection connection) throws IOException {
        try (InputStream inputStream = connection.getErrorStream()) {
            return new String(inputStream.readAllBytes()).trim();
        }
    }

    private static File createSqlTraceListenerLib() throws IOException {
        JavaArchive javaArchive = ShrinkWrap.create(JavaArchive.class)
                .addClasses(LastTraceSQLTraceListener.class);

        LOG.log(INFO, javaArchive.toString(true));

        File appLib = new File(appLibDir, LIB_FILE_NAME);
        javaArchive.as(ZipExporter.class).exportTo(appLib, true);
        return appLib;
    }

    private static File createWebApp() throws IOException {
        WebArchive webArchive = ShrinkWrap.create(WebArchive.class)
                .addClass(SqlListenerApplication.class)
                .addClass(SqlListenerEndpoint.class)
                .addClass(Employee.class)
                .addAsResource(new File(RESOURCE_ROOT, "/META-INF/persistence.xml"), "/META-INF/persistence.xml")
                .addAsResource(new File(RESOURCE_ROOT, "/META-INF/load.sql"), "/META-INF/load.sql");

        LOG.log(INFO, webArchive.toString(true));

        File webApp = new File(webAppDir, WEBAPP_FILE_NAME);
        webArchive.as(ZipExporter.class).exportTo(webApp, true);
        return webApp;
    }
}
