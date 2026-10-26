/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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
package org.glassfish.main.test.app.mphealth;

import jakarta.json.Json;
import jakarta.json.JsonReader;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;

import org.glassfish.common.util.HttpParser;
import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.test.app.mphealth.webapp.LivenessCheck;
import org.glassfish.main.test.app.mphealth.webapp.PlainServlet;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
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
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * Verifies that the MicroProfile Health endpoint works even if no deployed application uses CDI.
 *
 * @see <a href="https://github.com/eclipse-ee4j/glassfish/issues/26255">issue 26255</a>
 */
@TestMethodOrder(OrderAnnotation.class)
public class MpHealthTest {

    private static final System.Logger LOG = System.getLogger(MpHealthTest.class.getName());

    private static final String PLAIN_APP_NAME = "mphealth-plain";
    private static final String CDI_APP_NAME = "mphealth-cdi";

    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();

    @TempDir
    private static File tempDir;

    @BeforeAll
    public static void deployPlainApp() {
        File war = createWar(PLAIN_APP_NAME, ShrinkWrap.create(WebArchive.class).addClass(PlainServlet.class));
        assertThat(ASADMIN.exec("deploy", "--name", PLAIN_APP_NAME, war.getAbsolutePath()), asadminOK());
    }

    @AfterAll
    public static void undeployApps() {
        ASADMIN.exec("undeploy", CDI_APP_NAME);
        assertThat(ASADMIN.exec("undeploy", PLAIN_APP_NAME), asadminOK());
    }

    @Test
    @Order(1)
    public void healthWithoutCdiApplication() throws IOException {
        // Other tests may have deployed CDI applications into this server instance before,
        // so restart it to load just the application without CDI.
        assertThat(ASADMIN.exec("restart-domain", "domain1"), asadminOK());
        assertThat(get("/" + PLAIN_APP_NAME + "/plain"), equalTo("plain"));
        assertAll(
            () -> assertThat(status(get("/health")), equalTo("UP")),
            () -> assertThat(status(get("/health/live")), equalTo("UP")),
            () -> assertThat(status(get("/health/ready")), equalTo("UP")),
            () -> assertThat(status(get("/health/started")), equalTo("UP")));
    }

    @Test
    @Order(2)
    public void healthChecksOfCdiApplication() throws IOException {
        File war = createWar(CDI_APP_NAME, ShrinkWrap.create(WebArchive.class).addClass(LivenessCheck.class));
        assertThat(ASADMIN.exec("deploy", "--name", CDI_APP_NAME, war.getAbsolutePath()), asadminOK());
        assertThat(get("/health/live"), containsString("\"name\":\"" + LivenessCheck.NAME + "\""));

        assertThat(ASADMIN.exec("undeploy", CDI_APP_NAME), asadminOK());
        assertThat(get("/health/live"), not(containsString(LivenessCheck.NAME)));
    }

    private static String get(String path) throws IOException {
        HttpURLConnection connection = openConnection(8080, path);
        connection.setRequestMethod("GET");
        try {
            assertThat("HTTP status of " + path, connection.getResponseCode(), equalTo(200));
            return HttpParser.readResponseInputStream(connection).strip();
        } finally {
            connection.disconnect();
        }
    }

    private static String status(String healthReport) {
        try (JsonReader reader = Json.createReader(new StringReader(healthReport))) {
            return reader.readObject().getString("status");
        }
    }

    private static File createWar(String appName, WebArchive webArchive) {
        LOG.log(INFO, webArchive.toString(true));
        File warFile = new File(tempDir, appName + ".war");
        webArchive.as(ZipExporter.class).exportTo(warFile, true);
        return warFile;
    }
}
