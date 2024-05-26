/*
 * Copyright (c) 2023,2024 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.test.app.persistence.transform;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Files;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;

import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.WARNING;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.openConnection;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.glassfish.main.test.app.persistence.transform.webapp.ClassTransformerApplication;
import org.glassfish.main.test.app.persistence.transform.webapp.ClassTransformerResource;

/**
 * Tests bytecode preprocessing in WebappClassLoader.
 */
public class ClassTransformerWebAppTest extends ClassTransformerTestBase {

    private static final Class<?> TEST_CLASS = ClassTransformerWebAppTest.class;

    private static final Package TEST_PACKAGE = TEST_CLASS.getPackage();

    private static final System.Logger LOG = System.getLogger(TEST_CLASS.getName());

    private static final String APP_NAME = "TransformWebApp";

    private static final String CONTEXT_ROOT = "/" + APP_NAME;

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
            .addAsLibrary(createProvider())
            .addClass(ClassTransformerResource.class)
            .addClass(ClassTransformerApplication.class)
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
            .addAsResource(TEST_PACKAGE, "persistence.xml", "META-INF/persistence.xml");

        LOG.log(INFO, webArchive.toString(true));

        return createDeploymentFile(webArchive, APP_NAME);
    }
}
