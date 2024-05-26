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
import java.nio.file.Files;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;

import static java.lang.System.Logger.Level.WARNING;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.getAsadmin;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.openConnection;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.test.app.persistence.resourceref.webapp.ResourceRefApplication;
import org.glassfish.main.test.app.persistence.resourceref.webapp.ResourceRefResource;
import org.glassfish.main.test.setup.DeploymentAware;

/**
 * Tests that JTA datasource in persistence.xml can be a resource reference
 */
public class JtaDataSourceResourceRefTest implements DeploymentAware {

    private static final Class<?> TEST_CLASS = JtaDataSourceResourceRefTest.class;

    private static final Package TEST_PACKAGE = TEST_CLASS.getPackage();

    private static final System.Logger LOG = System.getLogger(TEST_CLASS.getName());

    private static final String APP_NAME = TEST_CLASS.getSimpleName() + "WebApp";

    private static final String CONTEXT_ROOT = "/" + APP_NAME;

    protected static final Asadmin ASADMIN = getAsadmin();

    public System.Logger getLogger() {
        return LOG;
    }

    @Test
    public void testDeploy() throws IOException {
        File warFile = createDeployment();
        try {
            assertThat(ASADMIN.exec("deploy", warFile.getAbsolutePath()), asadminOK());
        } finally {
            try {
                Files.deleteIfExists(warFile.toPath());
            } catch (IOException e) {
                LOG.log(WARNING, "An error occurred while remove temp file " + warFile.getAbsolutePath(), e);
            }
        }

        HttpURLConnection connection = openConnection(8080, CONTEXT_ROOT);
        connection.setRequestMethod("GET");
        try {
            assertThat(connection.getResponseCode(), equalTo(200));
        } finally {
            connection.disconnect();
        }

        assertThat(ASADMIN.exec("undeploy", APP_NAME), asadminOK());
    }

    private File createDeployment() throws IOException {
        WebArchive webArchive = ShrinkWrap.create(WebArchive.class)
            .addClass(ResourceRefResource.class)
            .addClass(ResourceRefApplication.class)
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
            .addAsWebInfResource(TEST_PACKAGE, "web.xml", "web.xml")
            .addAsResource(TEST_PACKAGE, "persistence.xml", "META-INF/persistence.xml");

        return createDeploymentWar(webArchive, APP_NAME);
    }
}
