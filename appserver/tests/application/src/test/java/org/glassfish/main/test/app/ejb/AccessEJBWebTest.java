/*
 * Copyright (c) 2023, 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.test.app.ejb;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;

import org.glassfish.common.util.HttpParser;
import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.RandomGenerator;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.itest.tools.asadmin.AsadminResult;
import org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static java.lang.System.Logger.Level.INFO;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.openConnection;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AccessEJBWebTest {
    private static final System.Logger LOG = System.getLogger(AccessEJBWebTest.class.getName());
    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();
    private static final String APP_NAME = "echoservice";

    @BeforeAll
    static void deploy() {
        File war = createDeployment();
        try {
            AsadminResult result = ASADMIN.exec("deploy", "--contextroot", "/", "--name", APP_NAME,
                    war.getAbsolutePath());
            assertThat(result, AsadminResultMatcher.asadminOK());
        } finally {
            war.delete();
        }
    }

    @AfterAll
    static void undeploy() {
        AsadminResult result = ASADMIN.exec("undeploy", APP_NAME);
        assertThat(result, AsadminResultMatcher.asadminOK());
    }

    @Test
    void testAccessLocalEJBByCDI() throws Exception {
        String message = RandomGenerator.generateRandomString();
        HttpURLConnection connection = openConnection(8080, "/local_ejb_cdi?message=" + message);
        try {
            connection.setRequestMethod("GET");
            assertAll(
                () -> assertEquals(200, connection.getResponseCode()),
                () -> assertEquals(message, HttpParser.readResponseInputStream(connection).trim())
            );
        } finally {
            connection.disconnect();
        }
    }

    @Test
    void testAccessLocalEJBByJNDI() throws Exception {
        String message = RandomGenerator.generateRandomString();
        HttpURLConnection connection = openConnection(8080, "/local_ejb_jndi?message=" + message);
        try {
            connection.setRequestMethod("GET");
            assertAll(
                () -> assertEquals(200, connection.getResponseCode()),
                () -> assertEquals(message, HttpParser.readResponseInputStream(connection).trim())
            );
        } finally {
            connection.disconnect();
        }
    }

    @Test
    void testAccessRemoteEJBByCDI() throws Exception {
        String message = RandomGenerator.generateRandomString();
        HttpURLConnection connection = openConnection(8080, "/remote_ejb_cdi?message=" + message);
        try {
            connection.setRequestMethod("GET");
            assertAll(
                () -> assertEquals(200, connection.getResponseCode()),
                () -> assertEquals(message, HttpParser.readResponseInputStream(connection).trim())
            );
        } finally {
            connection.disconnect();
        }
    }

    @Test
    void testAccessRemoteEJBByJNDI() throws Exception {
        String message = RandomGenerator.generateRandomString();
        HttpURLConnection connection = openConnection(8080, "/remote_ejb_jndi?message=" + message);
        try {
            connection.setRequestMethod("GET");
            assertAll(
                () -> assertEquals(200, connection.getResponseCode()),
                () -> assertEquals(message, HttpParser.readResponseInputStream(connection).trim())
            );
        } finally {
            connection.disconnect();
        }
    }

    private static File createDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, APP_NAME + ".war")
                .addClasses(EchoService.class, EchoServiceLocal.class, EchoServiceRemote.class, EchoServiceEJB.class)
                .addClasses(AccessLocalEJBByCDIServlet.class, AccessLocalEJBByJNDIServlet.class)
                .addClasses(AccessRemoteEJBByCDIServlet.class, AccessRemoteEJBByJNDIServlet.class);
        LOG.log(INFO, war.toString(true));
        try {
            File tempFile = File.createTempFile(APP_NAME, ".war");
            war.as(ZipExporter.class).exportTo(tempFile, true);
            return tempFile;
        } catch (IOException e) {
            throw new IllegalStateException("Deployment failed - cannot load the input archive!", e);
        }
    }
}
