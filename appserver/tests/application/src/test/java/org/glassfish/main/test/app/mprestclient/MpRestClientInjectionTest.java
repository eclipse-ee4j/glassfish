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

package org.glassfish.main.test.app.mprestclient;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;

import org.glassfish.common.util.HttpParser;
import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.test.app.mprestclient.webapp.GreetingClient;
import org.glassfish.main.test.app.mprestclient.webapp.GreetingEndpoint;
import org.glassfish.main.test.app.mprestclient.webapp.RestClientApplication;
import org.glassfish.main.test.app.mprestclient.webapp.RestClientEndpoint;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
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

/**
 * Deploys an application with a {@code @RegisterRestClient} interface injected as a CDI bean.
 * <p>
 * The MicroProfile Rest Client API bundle must see the CDI packages exported by the server,
 * otherwise Jersey's {@code RestClientExtension} fails while registering the client bean.
 */
public class MpRestClientInjectionTest {

    private static final System.Logger LOG = System.getLogger(MpRestClientInjectionTest.class.getName());

    private static final String APP_NAME = "mprestclient-app";

    private static final String CONTEXT_ROOT = "/mprestclient";

    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();

    @TempDir
    private static File tempDir;

    @BeforeAll
    static void deploy() {
        File warFile = createWebApp();
        assertThat(ASADMIN.exec("deploy", "--force", "--contextroot", CONTEXT_ROOT, "--name", APP_NAME,
            warFile.getAbsolutePath()), asadminOK());
    }

    @AfterAll
    static void undeploy() {
        assertThat(ASADMIN.exec("undeploy", APP_NAME), asadminOK());
    }

    @Test
    void injectedRestClientCallsRemoteEndpoint() throws IOException {
        HttpURLConnection connection = openConnection(8080, CONTEXT_ROOT + "/client");
        connection.setRequestMethod("GET");
        try {
            assertThat(connection.getResponseCode(), equalTo(200));
            assertThat(HttpParser.readResponseInputStream(connection), equalTo("Hello from the REST client"));
        } finally {
            connection.disconnect();
        }
    }

    private static File createWebApp() {
        String configProperties = "greeting/mp-rest/url=http://localhost:8080" + CONTEXT_ROOT;

        WebArchive webApp = ShrinkWrap.create(WebArchive.class)
            .addClass(RestClientApplication.class)
            .addClass(GreetingEndpoint.class)
            .addClass(GreetingClient.class)
            .addClass(RestClientEndpoint.class)
            .addAsResource(new StringAsset(configProperties), "META-INF/microprofile-config.properties");

        LOG.log(INFO, () -> webApp.toString(true));

        File warFile = new File(tempDir, APP_NAME + ".war");
        webApp.as(ZipExporter.class).exportTo(warFile, true);
        return warFile;
    }
}
