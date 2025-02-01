/*
 * Copyright (c) 2025 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.test.app.security.jakartasecurity;


import java.io.File;
import java.lang.System.Logger;
import java.net.http.HttpResponse;

import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.TestUtilities;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static java.lang.System.Logger.Level.INFO;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.getHttpResource;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class JakartaSecurityTest {

    private static final Logger LOG = System.getLogger(JakartaSecurityTest.class.getName());

    private static final String APP_NAME = "jakarta-security";

    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();

    @TempDir
    private static File tempDir;
    private static File warFile;

    @BeforeAll
    public static void prepareDeployment() throws Exception {
        final WebArchive webArchive = ShrinkWrap.create(WebArchive.class, APP_NAME + ".war")
            .addPackage(CustomPrincipal.class.getPackage());
        LOG.log(INFO, webArchive.toString(true));

        warFile = new File(tempDir, APP_NAME + ".war");
        webArchive.as(ZipExporter.class).exportTo(warFile, true);
        assertThat(ASADMIN.exec("deploy", "--target", "server", warFile.getAbsolutePath()), asadminOK());
    }

    @AfterAll
    public static void cleanup() throws Exception {
        ASADMIN.exec("undeploy", APP_NAME);
        TestUtilities.delete(warFile);
    }

    @Test
    void testCustomPrincipal() throws Exception {
        final HttpResponse<String> rootResponse = getHttpResource(false, 8080, "/" + APP_NAME + "/customPrincipal");
        assertEquals(200, rootResponse.statusCode(), "Response status code");
        assertTrue(rootResponse.body().contains(CustomPrincipal.class.getName()), "Response body: " + rootResponse.body());
    }

    @Test
    void testSerializableCoreSecurityContext() throws Exception {
        final HttpResponse<String> rootResponse = getHttpResource(false, 8080, "/" + APP_NAME + "/serializableCoreSecurityContext");
        assertAll(
            () -> assertEquals(200, rootResponse.statusCode(), "Response status code"),
            () -> assertTrue(rootResponse.body().contains("OK"), "Response body: " + rootResponse.body())
        );
    }

}
