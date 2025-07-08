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

package org.glassfish.main.test.app.deploy.afterfailed;

import java.io.File;

import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.test.app.deploy.BeanFailingDeployment;
import org.glassfish.main.test.app.deploy.BeanNotFailingDeployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.UnknownExtensionTypeException;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static java.lang.System.Logger.Level.INFO;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.getAsadmin;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminError;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.MatcherAssert.assertThat;

public class DeployAfterFailedDeploymentTest {

    private static final System.Logger LOG = System.getLogger(DeployAfterFailedDeploymentTest.class.getName());

    private static final Package TEST_PACKAGE = DeployAfterFailedDeploymentTest.class.getPackage();
    private static final String FAILING_APP_NAME = DeployAfterFailedDeploymentTest.class.getSimpleName() + "FailingApp";
    private static final String OK_APP_NAME = DeployAfterFailedDeploymentTest.class.getSimpleName() + "OKApp";

    private static final Asadmin ASADMIN = getAsadmin();

    @TempDir
    private static File tempDir;
    private static File failingAppWarFile;
    private static File okAppWarFile;

    @BeforeAll
    public static void prepareDeployment() {
        failingAppWarFile = prepareFailingApp();
        okAppWarFile = prepareOkApp();
    }

    private static File prepareFailingApp() throws IllegalArgumentException, UnknownExtensionTypeException {
        WebArchive webArchive = ShrinkWrap.create(WebArchive.class)
                .addClass(BeanFailingDeployment.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource(TEST_PACKAGE, "persistence.xml", "META-INF/persistence.xml");
        LOG.log(INFO, webArchive.toString(true));
        File warFile = new File(tempDir, FAILING_APP_NAME + ".war");
        webArchive.as(ZipExporter.class).exportTo(warFile, true);
        return warFile;
    }

    private static File prepareOkApp() {
        WebArchive webArchive = ShrinkWrap.create(WebArchive.class)
                .addClass(BeanNotFailingDeployment.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource(TEST_PACKAGE, "persistence.xml", "META-INF/persistence.xml");
        LOG.log(INFO, webArchive.toString(true));
        File warFile = new File(tempDir, OK_APP_NAME + ".war");
        webArchive.as(ZipExporter.class).exportTo(warFile, true);
        return warFile;
    }

    /**
     * This test verifies that the JNDI environment is cleaned up for the application context in case of failed deployment.
     * It used to happen that after a failed deployment, JNDI environment for the app name and context root stayed in memory,
     * with an entity manager factory that was closed after failed deployment.
     * A new deployment of the same app would pick up the previous JNDI environment and would attempt
     * to use the already closed entity manager factory, until server restarted.
     */
    @Test
    public void testDeployFailedAppAndThenFixedAppWithSameNameAndContextRoot() {
        assertThat(ASADMIN.exec("deploy", "--name", OK_APP_NAME, "--contextroot", "/", failingAppWarFile.getAbsolutePath()), asadminError("Initialization failed for Singleton BeanFailingDeployment"));
        assertThat(ASADMIN.exec("deploy", "--name", OK_APP_NAME, "--contextroot", "/", okAppWarFile.getAbsolutePath()), asadminOK());
        assertThat(ASADMIN.exec("undeploy", OK_APP_NAME), asadminOK());
    }

}
