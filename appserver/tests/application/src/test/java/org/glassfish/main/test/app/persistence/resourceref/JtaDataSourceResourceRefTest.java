/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
package org.glassfish.main.test.app.persistence.resourceref;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;

import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.TestUtilities;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.itest.tools.asadmin.AsadminResult;
import org.glassfish.main.itest.tools.asadmin.DomainPropertiesBackup;
import org.glassfish.main.test.app.persistence.resourceref.webapp.ResourceRefApplication;
import org.glassfish.main.test.app.persistence.resourceref.webapp.ResourceRefResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static java.lang.System.Logger.Level.INFO;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.getAsadmin;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.openConnection;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests that JTA datasource in persistence.xml can be a resource reference
 */
public class JtaDataSourceResourceRefTest {

    private static final System.Logger LOG = System.getLogger(JtaDataSourceResourceRefTest.class.getName());
    private static final Package TEST_PACKAGE = JtaDataSourceResourceRefTest.class.getPackage();
    private static final String APP_NAME = JtaDataSourceResourceRefTest.class.getSimpleName() + "WebApp";
    private static final String CONTEXT_ROOT = "/";
    private static final Asadmin ASADMIN = getAsadmin();

    private static final DomainPropertiesBackup DERBYPOOL_BACKUP = DomainPropertiesBackup.backupDerbyPool();

    @BeforeAll
    public static void deploy() throws Exception {
        GlassFishTestEnvironment.switchDerbyPoolToEmbededded();
        final File warFile = createDeployment();
        try {
            AsadminResult result = ASADMIN.exec("deploy", "--contextroot", CONTEXT_ROOT, "--name", APP_NAME,
                warFile.getAbsolutePath());
            assertThat(result, asadminOK());
        } finally {
            TestUtilities.delete(warFile);
        }
    }


    @AfterAll
    static void undeploy() {
        AsadminResult result = ASADMIN.exec("undeploy", APP_NAME);
        assertThat(result, asadminOK());
        DERBYPOOL_BACKUP.restore();
    }


    @Test
    public void test() throws IOException {
        final HttpURLConnection connection = openConnection(8080, CONTEXT_ROOT);
        connection.setRequestMethod("GET");
        try {
            assertThat(connection.getResponseCode(), equalTo(200));
        } finally {
            connection.disconnect();
        }
    }

    private static File createDeployment() throws IOException {
        final WebArchive webArchive = ShrinkWrap.create(WebArchive.class)
                .addClass(ResourceRefResource.class)
                .addClass(ResourceRefApplication.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsWebInfResource(TEST_PACKAGE, "web.xml", "web.xml")
                .addAsResource(TEST_PACKAGE, "persistence.xml", "META-INF/persistence.xml");
        LOG.log(INFO, webArchive.toString(true));
        File tempFile = File.createTempFile(APP_NAME, ".war");
        webArchive.as(ZipExporter.class).exportTo(tempFile, true);
        return tempFile;
    }
}
