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
package org.glassfish.main.test.app.persistence.data.repository;

import java.io.File;
import java.net.http.HttpResponse;

import org.glassfish.main.itest.tools.TestUtilities;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.itest.tools.asadmin.DomainPropertiesBackup;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static java.lang.System.Logger.Level.INFO;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.getAsadmin;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.getHttpResource;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.switchDerbyPoolToEmbededded;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * Smoketest using a minimal Jakarta Data application with a default Jakarta Persisence data source.
 */
public class DataRepositoryTest {

    private static final System.Logger LOG = System.getLogger(DataRepositoryTest.class.getName());
    private static final Package TEST_PACKAGE = FooRepository.class.getPackage();
    private static final String APP_NAME = DataRepositoryTest.class.getSimpleName() + "WebApp";
    private static final Asadmin ASADMIN = getAsadmin();

    private static final DomainPropertiesBackup DERBYPOOL_BACKUP = DomainPropertiesBackup.backupDerbyPool();

    @TempDir
    private static File tempDir;
    private static File warFile;

    @BeforeAll
    public static void deploy() throws Exception {
        switchDerbyPoolToEmbededded();

        WebArchive webArchive = ShrinkWrap.create(WebArchive.class)
                .addPackage(TEST_PACKAGE).deleteClass(DataRepositoryTest.class)
                .addAsResource(TEST_PACKAGE, "persistence.xml", "META-INF/persistence.xml");

        LOG.log(INFO, webArchive.toString(true));

        warFile = new File(tempDir, APP_NAME + ".war");
        webArchive.as(ZipExporter.class).exportTo(warFile, true);

        assertThat(ASADMIN.exec("deploy", "--target", "server", warFile.getAbsolutePath()), asadminOK());
    }

    @Test
    public void test() throws Exception {
        final HttpResponse<String> rootResponse = getHttpResource(APP_NAME + "/TestServlet");

        assertAll(
            () -> assertThat(rootResponse.statusCode(), equalTo(200)),
            () -> assertThat(rootResponse.body(), equalTo("doGet with myfoo"))
        );
    }

    @AfterAll
    public static void cleanup() throws Exception {
        assertThat(ASADMIN.exec("undeploy", APP_NAME), asadminOK());
        TestUtilities.delete(warFile);
        DERBYPOOL_BACKUP.restore();
    }

}
