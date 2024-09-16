/*
 * Copyright (c) 2023, 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.main.test.app.web.jsp;

import com.sun.enterprise.util.Utility;

import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;

import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.itest.tools.asadmin.AsadminResult;
import org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JspReloadGeneratedServletIfUpdatedTest {
    private static final Logger LOG = System.getLogger(JspReloadGeneratedServletIfUpdatedTest.class.getName());

    private static final String APP_NAME = JspReloadGeneratedServletIfUpdatedTest.class.getSimpleName();
    private static final String JSP_FILE_NAME = "JspReloadGeneratedServletIfUpdated";

    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();

    @BeforeAll
    static void deploy() {
        File war = createDeployment();
        try {
            AsadminResult result1 = ASADMIN.exec("deploy", "--contextroot", "/", "--name", APP_NAME, "--precompilejsp",
                "true", war.getAbsolutePath());
            assertThat(result1, AsadminResultMatcher.asadminOK());
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
    public void doTest() throws Exception {
        doHttpGet("This is my output");
        Path sourceFile = GlassFishTestEnvironment.getTargetDirectory().toPath().resolve(
            Path.of("classes", "org", "apache", "jsp", "reload", JSP_FILE_NAME + "_jsp.class"));
        Path targetFile = GlassFishTestEnvironment.getDomain1Directory().resolve(Path.of("generated", "jsp",
            APP_NAME, "org", "apache", "jsp", "reload", JSP_FILE_NAME + "_jsp.class"));
        assertTimeout(Duration.ofSeconds(10L), () -> assertTrue(targetFile.toFile().exists()));
        Files.createDirectories(targetFile.getParent());
        Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
        Files.setLastModifiedTime(targetFile, FileTime.from(Instant.now()));
        doHttpGet("This is my UPDATED output");
    }


    private void doHttpGet(String expectedText) throws Exception {
        HttpURLConnection connection = GlassFishTestEnvironment.openConnection(8080,
            "/reload/" + JSP_FILE_NAME + ".jsp");
        connection.setRequestMethod("GET");
        String line = Utility.readResponseInputStream(connection).trim();
        assertAll(
            () -> assertEquals(200, connection.getResponseCode(), "Wrong response code."),
            () -> assertEquals(expectedText, line, "Wrong response body.")
        );
    }


    private static File createDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "JspReloadGeneratedServletIfUpdated.war")
            .addAsWebResource("org/glassfish/main/test/app/web/jsp/" + JSP_FILE_NAME + ".jsp",
                "reload/" + JSP_FILE_NAME + ".jsp")
            .addAsWebInfResource("org/glassfish/main/test/app/web/jsp/web.xml", "web.xml");
        LOG.log(Level.INFO, war.toString(true));
        try {
            File tempFile = File.createTempFile(APP_NAME, ".war");
            war.as(ZipExporter.class).exportTo(tempFile, true);
            return tempFile;
        } catch (IOException e) {
            throw new IllegalStateException("Deployment failed - cannot load the input archive!", e);
        }
    }
}
