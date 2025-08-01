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

package org.glassfish.main.test.app.ejblinksyntax;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Files;

import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.test.app.ejblinksyntax.api.Cat;
import org.glassfish.main.test.app.ejblinksyntax.api.Creature;
import org.glassfish.main.test.app.ejblinksyntax.base.Ant;
import org.glassfish.main.test.app.ejblinksyntax.dependson.modulename.Choco;
import org.glassfish.main.test.app.ejblinksyntax.war.InspectorServlet;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.WARNING;
import static org.glassfish.common.util.HttpParser.readResponseInputStream;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.openConnection;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class EjbDependsOnByModuleNameTest {

    private static final System.Logger LOG = System.getLogger(EjbDependsOnByModuleNameTest.class.getName());

    private static final Package TEST_PACKAGE = EjbDependsOnByModuleNameTest.class.getPackage();

    private static final String APP_NAME = "module-name-app";

    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();

    @TempDir
    private static File tempDir;

    @BeforeAll
    public static void deploy() throws IOException {
        File warFile = createDeployment();
        try {
            assertThat(ASADMIN.exec("deploy", warFile.getAbsolutePath()), asadminOK());
        } finally {
            try {
                Files.deleteIfExists(warFile.toPath());
            } catch (IOException e) {
                LOG.log(WARNING, "An error occurred while remove temporary file " + warFile.getAbsolutePath(), e);
            }
        }
    }

    @AfterAll
    public static void undeploy() {
        assertThat(ASADMIN.exec("undeploy", APP_NAME), asadminOK());
    }

    @Test
    public void testEjbLinkSyntax() throws Exception{
        HttpURLConnection connection = openConnection(8080, "/ejblinksyntax/inspector");
        connection.setRequestMethod("GET");
        assertAll(
            () -> assertEquals(200, connection.getResponseCode()),
            () -> assertEquals(
                "Cat of color: brown (module-name/) and creature with name: Z",
                readResponseInputStream(connection)
            )
        );
    }

    private static File createDeployment() {
        JavaArchive apiArchive = ShrinkWrap.create(JavaArchive.class, "api.jar")
            .addClass(Cat.class)
            .addClass(Creature.class);

        LOG.log(INFO, apiArchive.toString(true));

        JavaArchive ejbBaseArchive = ShrinkWrap.create(JavaArchive.class, "ejb-base.jar")
            .addClass(Ant.class)
            .addAsManifestResource(TEST_PACKAGE, "ejb-jar.xml", "ejb-jar.xml");

        LOG.log(INFO, ejbBaseArchive.toString(true));

        JavaArchive ejbArchive = ShrinkWrap.create(JavaArchive.class, "ejb-module-name.jar")
            .addClass(Choco.class);

        LOG.log(INFO, ejbArchive.toString(true));

        WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "webapp.war")
            .addClass(InspectorServlet.class);

        LOG.log(INFO, webArchive.toString(true));

        EnterpriseArchive enterpriseArchive = ShrinkWrap.create(EnterpriseArchive.class)
            .addAsLibrary(apiArchive)
            .addAsModule(ejbBaseArchive)
            .addAsModule(ejbArchive)
            .addAsModule(webArchive)
            .setApplicationXML(TEST_PACKAGE, "application.xml");

        LOG.log(INFO, enterpriseArchive.toString(true));

        File earFile = new File(tempDir, "module-name-app.ear");
        enterpriseArchive.as(ZipExporter.class).exportTo(earFile, true);

        return earFile;
    }
}
