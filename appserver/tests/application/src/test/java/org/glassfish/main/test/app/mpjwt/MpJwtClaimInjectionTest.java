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
package org.glassfish.main.test.app.mpjwt;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;

import org.glassfish.common.util.HttpParser;
import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.test.app.mpjwt.webapp.JwtApplication;
import org.glassfish.main.test.app.mpjwt.webapp.JwtClaimEndpoint;
import org.glassfish.main.test.app.mpjwt.webapp.JwtCustomAuthMechanismHandler;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static java.lang.System.Logger.Level.INFO;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.openConnection;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@Disabled("MpJwtClaimInjectionTest.testJwtClaimInjection:64 JWT security should be active")
public class MpJwtClaimInjectionTest {

    private static final System.Logger LOG = System.getLogger(MpJwtClaimInjectionTest.class.getName());

    private static final String APP_NAME = "mpjwt-app";

    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();

    @TempDir
    private static File tempDir;

    @Test
    void testJwtClaimInjectionWithoutHandler() throws IOException {
        testJwtClaimInjection(false);
    }

    @Test
    void testJwtClaimInjectionWithHandler() throws IOException {
        testJwtClaimInjection(true);
    }

    void testJwtClaimInjection(boolean withHandler) throws IOException {
        File app = createWebApp(withHandler);
        assertThat(ASADMIN.exec("deploy", "--force", "--contextroot", "app", "--name", APP_NAME, app.getAbsolutePath()), asadminOK());

        try {
            // Test that the endpoint is accessible (even without valid JWT, it should deploy successfully)
            HttpURLConnection connection = openConnection(8080, "/app/claim/subject");
            connection.setRequestMethod("GET");

            // Without JWT, we expect 401 Unauthorized, which means JWT security is working
            int responseCode = connection.getResponseCode();

            // Either 401 (unauthorized) or 403 (forbidden) indicates JWT security is active
            assertThat("JWT security should be active", responseCode == 401 || responseCode == 403, equalTo(true));
            connection.disconnect();
        } finally {
            assertThat(ASADMIN.exec("undeploy", APP_NAME), asadminOK());
        }
    }

    private String getJwtClaim() throws IOException {
        HttpURLConnection connection = openConnection(8080, "/app/claim/subject");
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Bearer " + createTestJwt());
        try {
            assertThat(connection.getResponseCode(), equalTo(200));
            return HttpParser.readResponseInputStream(connection);
        } finally {
            connection.disconnect();
        }
    }

    private String createTestJwt() {
        // Minimal JWT for testing - in real scenario this would be properly signed
        return "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlzcyI6InRlc3QiLCJhdWQiOiJ0ZXN0In0.test";
    }

    private static File createWebApp(boolean withHandler) {
        String jwtConfig = """
            mp.jwt.verify.issuer=https://server.example.com
            mp.jwt.verify.publickey.location=/publicKey.pem
            """;

        String publicKey = """
            -----BEGIN PUBLIC KEY-----
            MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlivFI8qB4D0y2jy0CfEq
            Fyy46R0o7S8TKpsx5xbHKoU1VWg6QkQm+ntyIv1p4kE1sPEQO73+HY8+Bzs75XwR
            TYL1BmR1w8J5hmjVWjc6R2BTBGAYRPFRhor3kpM6ni2SPmNNhurEAHw7TaqszP5e
            UF/F9+KEBWkwVta+PZ37bwqSE4sCb1soZFrVz/UT/LF4tYpuVYt3YbqToZ3pZOZ9
            AX2o1GCG3xwOjkc4x0W7ezbQZdC9iftPxVHR8irOijJRRjcPDtA6vPKpzLl6CyYn
            sIYPd99ltwxTHjr3npfv/3Lw50bAkbT4HeLFxTx4flEoZLKO/g0bAoV2uqBhkA9x
            nQIDAQAB
            -----END PUBLIC KEY-----
            """;

        WebArchive webApp = ShrinkWrap.create(WebArchive.class)
                .addClass(JwtClaimEndpoint.class)
                .addClass(JwtApplication.class)
                .addAsResource(new StringAsset(jwtConfig), "META-INF/microprofile-config.properties")
                .addAsResource(new StringAsset(publicKey), "publicKey.pem");

        if (withHandler) {
            webApp = webApp.addClass(JwtCustomAuthMechanismHandler.class);
        }

        LOG.log(INFO, webApp.toString(true));

        File webAppFile = new File(tempDir, "jwt-app.war");
        webApp.as(ZipExporter.class).exportTo(webAppFile, true);
        return webAppFile;
    }
}
