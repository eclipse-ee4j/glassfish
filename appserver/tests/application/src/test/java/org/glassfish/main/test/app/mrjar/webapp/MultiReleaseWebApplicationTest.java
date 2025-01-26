/*
 * Copyright (c) 2023, 2024 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.test.app.mrjar.webapp;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Files;

import org.glassfish.common.util.HttpParser;
import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.test.app.mrjar.MultiReleaseTestBase;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.condition.EnabledForJreRange;

import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.WARNING;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.condition.JRE.JAVA_11;
import static org.junit.jupiter.api.condition.JRE.JAVA_16;
import static org.junit.jupiter.api.condition.JRE.JAVA_17;
import static org.objectweb.asm.Opcodes.V11;
import static org.objectweb.asm.Opcodes.V17;

public class MultiReleaseWebApplicationTest extends MultiReleaseTestBase {

    private static final System.Logger LOG = System.getLogger(MultiReleaseWebApplicationTest.class.getName());

    private static final String APP_NAME = "mrwebapp";

    private static final String CONTEXT_ROOT = "/" + APP_NAME;

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
    @EnabledForJreRange(min = JAVA_11, max = JAVA_16)
    public void testMultiReleaseJarProcessingJdk11(TestInfo testInfo) throws IOException {
        LOG.log(INFO, "Run test method {0}", testInfo.getTestMethod().orElseThrow().getName());
        HttpURLConnection connection = GlassFishTestEnvironment.openConnection(8080, CONTEXT_ROOT);
        connection.setRequestMethod("GET");
        try {
            assertAll(
                () -> assertThat(connection.getResponseCode(), equalTo(200)),
                // Check version of loaded class file
                () -> assertThat(Integer.parseInt(HttpParser.readResponseInputStream(connection)), equalTo(V11))
            );
        } finally {
            connection.disconnect();
        }
    }

    @Test
    @EnabledForJreRange(min = JAVA_17)
    public void testMultiReleaseJarProcessingJdk17(TestInfo testInfo) throws IOException {
        LOG.log(INFO, "Run test method {0}", testInfo.getTestMethod().orElseThrow().getName());
        HttpURLConnection connection = GlassFishTestEnvironment.openConnection(8080, CONTEXT_ROOT);
        connection.setRequestMethod("GET");
        try {
            assertAll(
                () -> assertThat(connection.getResponseCode(), equalTo(200)),
                // Check version of loaded class file
                () -> assertThat(Integer.parseInt(HttpParser.readResponseInputStream(connection)), equalTo(V17))
            );
        } finally {
            connection.disconnect();
        }
    }

    private static File createDeployment() throws IOException {
        // Create Multi-Release JAR library
        JavaArchive jarArchive = createMultiReleaseLibrary();

        // Create test web application archive
        WebArchive webArchive = ShrinkWrap.create(WebArchive.class)
            .addAsLibrary(jarArchive)
            .addClass(MultiReleaseResource.class)
            .addClass(MultiReleaseApplication.class);

        LOG.log(INFO, webArchive.toString(true));

        return createFileFor(webArchive, APP_NAME);
    }
}
