/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.test.app.mpconfig;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;

import org.glassfish.common.util.HttpParser;
import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.test.app.mpconfig.webapp.ConfigApplication;
import org.glassfish.main.test.app.mpconfig.webapp.ConfigWithoutInjectionEndpoint;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static java.lang.System.Logger.Level.INFO;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.openConnection;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class MpConfigIwthoutInjectionTest {

    private static final System.Logger LOG = System.getLogger(MpConfigIwthoutInjectionTest.class.getName());

    private static final String APP_NAME = "mpconfig-app";

    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();

    @TempDir
    private static File tempDir;

    @Test
    void testApplicationsDontShareConfigStoredInSharedStaticField() throws IOException {
        String appId = "testapp";
        File app1 = createWebApp(appId);
        final String appName = APP_NAME + "-" + appId;
        assertThat(ASADMIN.exec("deploy", "--force", "--contextroot", "app", "--name", appName, app1.getAbsolutePath()), asadminOK());

        String configValue = getConfigName();

        Assertions.assertAll(
                () -> assertThat(configValue, equalTo(appId)),
                () -> assertThat(ASADMIN.exec("undeploy", appName), asadminOK()));
    }

    private String getConfigName() throws IOException {
        HttpURLConnection connection = openConnection(8080, "/app/config/name");
        connection.setRequestMethod("GET");
        try {
            assertThat(connection.getResponseCode(), equalTo(200));
            return HttpParser.readResponseInputStream(connection);
        } finally {
            connection.disconnect();
        }
    }

    private static File createWebApp(String configValue) {
        String configProperties = "name=" + configValue;

        WebArchive webApp = ShrinkWrap.create(WebArchive.class)
            .addClass(ConfigWithoutInjectionEndpoint.class)
            .addClass(ConfigApplication.class)
                // Using microprofile-config.properties is essential for this test - it triggers ConfigDeployer
                // even if no Config annotation is in the app
            .addAsResource(new StringAsset(configProperties), "META-INF/microprofile-config.properties");

        LOG.log(INFO, webApp.toString(true));

        File webAppFile = new File(tempDir, "app-" + configValue + ".war");
        webApp.as(ZipExporter.class).exportTo(webAppFile, true);
        return webAppFile;
    }
}
