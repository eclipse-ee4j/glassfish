/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.test.app.signedear;

import java.io.File;
import java.lang.System.Logger.Level;
import java.util.UUID;

import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.JarSigner;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.jdke.security.KeyTool;
import org.glassfish.main.test.app.signedear.api.ExampleRemote;
import org.glassfish.main.test.app.signedear.impl.ExampleBean;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Integration test for deployment EAR application that contains signed
 * shared libraries and modules.
 * <p>
 * This integration test checks the correctness of the classloading
 * of generated classes.
 */
public class SignedEarDeploymentTest {

    private static final System.Logger LOG = System.getLogger(SignedEarDeploymentTest.class.getName());

    private static final String APP_NAME = "signed";

    private static final String KEYSTORE_PASSWORD = UUID.randomUUID().toString();

    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();
    private static final JarSigner JARSIGNER = GlassFishTestEnvironment.getJarSigner();

    @TempDir
    private static File tempDir;
    private static File earFile;

    @BeforeAll
    public static void prepareDeployment() throws Exception {
        File keyStore = new File(tempDir, "signtest.jks");
        new KeyTool(keyStore, KEYSTORE_PASSWORD.toCharArray()).generateKeyPair("signtest", "CN=SIGNTEST", "RSA", 7);

        // Create shared library.
        JavaArchive apiArchive = ShrinkWrap.create(JavaArchive.class)
            .addClass(ExampleRemote.class);
        File apiFile = new File(tempDir, "api.jar");
        apiArchive.as(ZipExporter.class).exportTo(apiFile);
        LOG.log(Level.INFO, apiArchive.toString(true));

        // Sign shared library.
        JARSIGNER.exec("-keystore", keyStore.getAbsolutePath(), "-storepass", KEYSTORE_PASSWORD,
            "-keypass", KEYSTORE_PASSWORD, apiFile.getAbsolutePath(), "signtest");

        // Create EAR EJB module.
        JavaArchive implArchive = ShrinkWrap.create(JavaArchive.class).addClass(ExampleBean.class);
        File implFile = new File(tempDir, "impl.jar");
        implArchive.as(ZipExporter.class).exportTo(implFile);
        LOG.log(Level.INFO, implArchive.toString(true));

        // Sign EAR EJB module.
        JARSIGNER.exec("-keystore", keyStore.getAbsolutePath(), "-storepass", KEYSTORE_PASSWORD,
            "-keypass", KEYSTORE_PASSWORD, implFile.getAbsolutePath(), "signtest");

        // Create EAR application.
        EnterpriseArchive enterpriseArchive = ShrinkWrap.create(EnterpriseArchive.class)
            .addAsLibrary(apiFile)
            .addAsModule(implFile);
        earFile = new File(tempDir, APP_NAME + ".ear");
        enterpriseArchive.as(ZipExporter.class).exportTo(earFile);
        LOG.log(Level.INFO, enterpriseArchive.toString(true));
    }

    @AfterAll
    public static void cleanup() {
        ASADMIN.exec("undeploy", APP_NAME);
    }

    @Test
    public void testDeployment() {
        assertThat(ASADMIN.exec("deploy", earFile.getAbsolutePath()), asadminOK());
    }
}
