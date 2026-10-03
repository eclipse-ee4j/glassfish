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

package org.glassfish.main.test.app.cdi.startup;

import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.net.HttpURLConnection;

import org.glassfish.common.util.HttpParser;
import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.itest.tools.asadmin.AsadminResult;
import org.glassfish.main.test.app.cdi.startup.lib.LibraryStartupObserver;
import org.glassfish.main.test.app.cdi.startup.webapp.StartupStatusServlet;
import org.glassfish.main.test.app.cdi.startup.webapp.WebAppStartupObserver;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static java.lang.System.Logger.Level.INFO;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.openConnection;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

/**
 * The {@code Startup} event observed by a bean in {@code WEB-INF/lib} must not be fired before
 * the resources the web application defines are bound.
 */
class StartupEventInWebLibraryTest {

    private static final Logger LOG = System.getLogger(StartupEventInWebLibraryTest.class.getName());
    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();
    private static final String APP_NAME = StartupEventInWebLibraryTest.class.getSimpleName();

    @BeforeAll
    static void deploy() throws IOException {
        File war = createDeployment();
        try {
            AsadminResult result = ASADMIN.exec("deploy", "--contextroot", "/" + APP_NAME, "--name", APP_NAME,
                war.getAbsolutePath());
            assertThat(result, asadminOK());
        } finally {
            war.delete();
        }
    }

    @AfterAll
    static void undeploy() {
        AsadminResult result = ASADMIN.exec("undeploy", APP_NAME);
        assertThat(result, asadminOK());
    }

    @Test
    void dataSourceAvailableInStartupObservers() throws Exception {
        HttpURLConnection connection = openConnection(8080, "/" + APP_NAME + "/status");
        connection.setRequestMethod("GET");
        try {
            assertThat(connection.getResponseCode(), equalTo(200));
            String response = HttpParser.readResponseInputStream(connection);
            assertThat(response, containsString("webapp=OK"));
            assertThat(response, containsString("library=OK"));
        } finally {
            connection.disconnect();
        }
    }

    private static File createDeployment() throws IOException {
        JavaArchive library = ShrinkWrap.create(JavaArchive.class, "startup-library.jar")
            .addClass(LibraryStartupObserver.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        WebArchive war = ShrinkWrap.create(WebArchive.class, APP_NAME + ".war")
            .addClasses(StartupStatusServlet.class, WebAppStartupObserver.class)
            .addAsLibrary(library);
        LOG.log(INFO, war.toString(true));
        File warFile = File.createTempFile(APP_NAME, ".war");
        war.as(ZipExporter.class).exportTo(warFile, true);
        return warFile;
    }
}
