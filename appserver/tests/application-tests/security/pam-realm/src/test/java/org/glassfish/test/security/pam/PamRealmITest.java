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

package org.glassfish.test.security.pam;

import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

import org.glassfish.test.security.pam.docker.GlassFishContainer;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.DockerClientFactory;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.condition.OS.WINDOWS;

@DisabledOnOs(WINDOWS)
public class PamRealmITest {

    private static final GlassFishContainer AS_DOMAIN = new GlassFishContainer("admin", "A");

    private static final String APP_NAME = "pam";

    @TempDir
    private static File tmpDir;

    private static WebTarget endpoint;

    @BeforeAll
    public static void start() {
        assumeTrue(DockerClientFactory.instance().isDockerAvailable(),
            "Docker is not available in this environment");
        AS_DOMAIN.start();
        endpoint = AS_DOMAIN.deploy(APP_NAME, getArchiveToDeploy());
    }

    @AfterAll
    public static void stop() {
        AS_DOMAIN.stop();
    }

    @Test
    public void testAllowedUserAccess() {
        Response response = endpoint.request(MediaType.TEXT_PLAIN)
            .header("Authorization", createBasicAuthHeader("user", "password")).get();
        assertThat(response.getStatus(), equalTo(200));
    }

    @Test
    public void testInvalidUserAccess() {
        Response response = endpoint.request(MediaType.TEXT_PLAIN)
            .header("Authorization", createBasicAuthHeader("invalid", "invalid")).get();
        assertThat(response.getStatus(), equalTo(401));
    }

    @Test
    public void testUnauthorizedAccess() {
        Response response = endpoint.request(MediaType.TEXT_PLAIN).get();
        assertThat(response.getStatus(), equalTo(401));
    }

    private static File getArchiveToDeploy() {
        WebArchive webArchive = ShrinkWrap.create(WebArchive.class)
            .addClass(HelloServlet.class)
            .addAsWebInfResource("web.xml", "web.xml");
        File warFile;
        try {
            warFile = File.createTempFile(APP_NAME, ".war", tmpDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        webArchive.as(ZipExporter.class).exportTo(warFile, true);
        return warFile;
    }

    private String createBasicAuthHeader(String user, String password) {
        String credentials = user + ":" + password;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
        return "Basic " + encodedCredentials;
    }
}
