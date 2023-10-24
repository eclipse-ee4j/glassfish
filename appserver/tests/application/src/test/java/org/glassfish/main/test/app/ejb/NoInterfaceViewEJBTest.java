/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
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
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.nio.file.Files;

import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static java.lang.String.format;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.WARNING;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.getAsadmin;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.openConnection;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class NoInterfaceViewEJBTest {

    private static final System.Logger LOG = System.getLogger(NoInterfaceViewEJBTest.class.getName());

    private static final String APP_NAME = "nointerfaceview";

    private static final String WAR_FILE_NAME = APP_NAME + ".war";

    private static final Asadmin ASADMIN = getAsadmin();

    @BeforeAll
    public static void deploy() throws IOException {
        File warFile = createDeployment();
        try {
            assertThat(ASADMIN.exec("deploy", warFile.getAbsolutePath()), asadminOK());
        } finally {
            try {
                Files.deleteIfExists(warFile.toPath());
            } catch (IOException e) {
                LOG.log(WARNING, "An error occurred while remove temporary file " + warFile.getAbsolutePath(), e);
            }
        }
    }

    @AfterAll
    public static void undeploy() {
        assertThat(ASADMIN.exec("undeploy", APP_NAME), asadminOK());
    }

    @Test
    public void testGetObject() throws IOException {
        HttpURLConnection connection = openConnection(8080, contextFor("object"));
        connection.setRequestMethod("GET");
        try {
            assertAll(
                () -> assertThat(connection.getResponseCode(), equalTo(200)),
                () -> assertThat(readResponse(connection), equalTo(NoInterfaceViewEJB.class.getName()))
            );
        } finally {
            connection.disconnect();
        }
    }

    @Test
    public void testGetBoolean() throws IOException {
        HttpURLConnection connection = openConnection(8080, contextFor("boolean"));
        connection.setRequestMethod("GET");
        try {
            assertAll(
                () -> assertThat(connection.getResponseCode(), equalTo(200)),
                () -> assertThat(readEntity(connection, Boolean.class), equalTo(Boolean.TRUE))
            );
        } finally {
            connection.disconnect();
        }
    }

    @Test
    public void testGetByte() throws IOException {
        HttpURLConnection connection = openConnection(8080, contextFor("byte"));
        connection.setRequestMethod("GET");
        try {
            assertAll(
                () -> assertThat(connection.getResponseCode(), equalTo(200)),
                () -> assertThat(readEntity(connection, Byte.class), equalTo(Byte.MAX_VALUE))
            );
        } finally {
            connection.disconnect();
        }
    }

    @Test
    public void testGetShort() throws IOException {
        HttpURLConnection connection = openConnection(8080, contextFor("short"));
        connection.setRequestMethod("GET");
        try {
            assertAll(
                () -> assertThat(connection.getResponseCode(), equalTo(200)),
                () -> assertThat(readEntity(connection, Short.class), equalTo(Short.MAX_VALUE))
            );
        } finally {
            connection.disconnect();
        }
    }

    @Test
    public void testGetInt() throws IOException {
        HttpURLConnection connection = openConnection(8080, contextFor("int"));
        connection.setRequestMethod("GET");
        try {
            assertAll(
                () -> assertThat(connection.getResponseCode(), equalTo(200)),
                () -> assertThat(readEntity(connection, Integer.class), equalTo(Integer.MAX_VALUE))
            );
        } finally {
            connection.disconnect();
        }
    }

    @Test
    public void testGetLong() throws IOException {
        HttpURLConnection connection = openConnection(8080, contextFor("long"));
        connection.setRequestMethod("GET");
        try {
            assertAll(
                () -> assertThat(connection.getResponseCode(), equalTo(200)),
                () -> assertThat(readEntity(connection, Long.class), equalTo(Long.MAX_VALUE))
            );
        } finally {
            connection.disconnect();
        }
    }

    @Test
    public void testGetFloat() throws IOException {
        HttpURLConnection connection = openConnection(8080, contextFor("float"));
        connection.setRequestMethod("GET");
        try {
            assertAll(
                () -> assertThat(connection.getResponseCode(), equalTo(200)),
                () -> assertThat(Float.compare(readEntity(connection, Float.class), Float.MAX_VALUE), equalTo(0))
            );
        } finally {
            connection.disconnect();
        }
    }

    @Test
    public void testGetDouble() throws IOException {
        HttpURLConnection connection = openConnection(8080, contextFor("double"));
        connection.setRequestMethod("GET");
        try {
            assertAll(
                () -> assertThat(connection.getResponseCode(), equalTo(200)),
                () -> assertThat(Double.compare(readEntity(connection, Double.class), Double.MAX_VALUE), equalTo(0))
            );
        } finally {
            connection.disconnect();
        }
    }

    @Test
    public void testCallVoidMethod() throws IOException {
        HttpURLConnection connection = openConnection(8080, contextFor("void"));
        connection.setRequestMethod("GET");
        try {
            assertThat(connection.getResponseCode(), equalTo(204));
        } finally {
            connection.disconnect();
        }
    }

    @Test
    public void testCallPackagePrivateMethod() throws IOException {
        HttpURLConnection connection = openConnection(8080, contextFor("package-private"));
        connection.setRequestMethod("GET");
        try {
            assertAll(
                () -> assertThat(connection.getResponseCode(), equalTo(200)),
                () -> assertThat(readResponse(connection),
                        containsString("Illegal non-business method access on no-interface view"))
            );
        } finally {
            connection.disconnect();
        }
    }

    @Test
    public void testCallProtectedMethod() throws IOException {
        HttpURLConnection connection = openConnection(8080, contextFor("protected"));
        connection.setRequestMethod("GET");
        try {
            assertAll(
                () -> assertThat(connection.getResponseCode(), equalTo(200)),
                () -> assertThat(readResponse(connection),
                        containsString("Illegal non-business method access on no-interface view"))
            );
        } finally {
            connection.disconnect();
        }
    }

    @Test
    public void testCallNonPublicStaticMethod() throws IOException {
        HttpURLConnection connection = openConnection(8080, contextFor("static"));
        connection.setRequestMethod("GET");
        try {
            assertThat(connection.getResponseCode(), equalTo(204));
        } finally {
            connection.disconnect();
        }
    }

    private static File createDeployment() throws IOException {
        // Create test web application archive
        WebArchive webArchive = ShrinkWrap.create(WebArchive.class)
            .addClass(NoInterfaceViewEJB.class)
            .addClass(NoInterfaceViewResource.class)
            .addClass(NoInterfaceViewApplication.class);

        LOG.log(INFO, webArchive.toString(true));

        File tmpDir = Files.createTempDirectory(APP_NAME).toFile();
        File warFile = new File(tmpDir, WAR_FILE_NAME);
        webArchive.as(ZipExporter.class).exportTo(warFile, true);
        tmpDir.deleteOnExit();
        return warFile;
    }

    private String contextFor(String path) {
        return format("/%s/%s", APP_NAME, path);
    }

    private String readResponse(URLConnection connection) throws IOException {
        try (InputStream in = connection.getInputStream()) {
            return new String(in.readAllBytes());
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T readEntity(URLConnection connection, Class<T> type) throws Exception {
        Method valueOf;
        try {
            valueOf = type.getDeclaredMethod("parse" + type.getSimpleName(), String.class);
        } catch (NoSuchMethodException e) {
            valueOf = type.getDeclaredMethod("valueOf", String.class);
        }
        return (T) valueOf.invoke(null, readResponse(connection));
    }
}
