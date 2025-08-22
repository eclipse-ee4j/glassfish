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

package org.glassfish.main.test.app.synchronize;

import java.io.File;

import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static java.lang.System.Logger.Level.INFO;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.getAsadmin;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests synchronization between DAS and standalone instance.
 */
public class SynchronizeStandaloneInstanceTest {

    private static final Class<?> TEST_CLASS = SynchronizeStandaloneInstanceTest.class;

    private static final Package TEST_PACKAGE = TEST_CLASS.getPackage();

    private static final System.Logger LOG = System.getLogger(TEST_CLASS.getName());

    private static final String APP_NAME = SynchronizeStandaloneInstanceTest.class.getSimpleName();

    private static final String INSTANCE_NAME = "testInstance";

    private static final Asadmin ASADMIN = getAsadmin();

    @TempDir
    private static File tempDir;

    @BeforeAll
    public static void createInstance() {
        try {
            assertThat(ASADMIN.exec("create-instance", "--node", "localhost-domain1", INSTANCE_NAME), asadminOK());
        } catch (AssertionError e) {
            // cleanup on error, the instance is not going to be deleted otherwise
            ASADMIN.exec("delete-instance", INSTANCE_NAME);
            throw e;
        }
        assertThat(ASADMIN.exec("stop-instance", INSTANCE_NAME), asadminOK());
        assertThat(ASADMIN.exec("deploy", "--target", INSTANCE_NAME, createDeployment().getAbsolutePath()), asadminOK());
    }

    @AfterAll
    public static void deleteInstance() {
        assertThat(ASADMIN.exec("undeploy", "--target", INSTANCE_NAME, APP_NAME), asadminOK());
        assertThat(ASADMIN.exec("stop-instance", INSTANCE_NAME), asadminOK());
        assertThat(ASADMIN.exec("delete-instance", INSTANCE_NAME), asadminOK());
    }

    @Test
    public void testSynchronization() {
        // Longer timeout for the synchronization
        assertThat(ASADMIN.exec("start-instance", "--timeout", "120", INSTANCE_NAME), asadminOK());
    }

    private static File createDeployment() {
        WebArchive webArchive = ShrinkWrap.create(WebArchive.class)
            .addAsWebInfResource(TEST_PACKAGE, "sample.json", "resources/sample.json")
            .addAsWebResource(TEST_PACKAGE, "index.html");

        LOG.log(INFO, webArchive.toString(true));

        File warFile = new File(tempDir, APP_NAME + ".war");
        webArchive.as(ZipExporter.class).exportTo(warFile, true);
        return warFile;
    }
}
