/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.test.app.deploy;

import java.io.File;

import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static java.lang.System.Logger.Level.INFO;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.getAsadmin;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.MatcherAssert.assertThat;

public class DeployOnVirtualServersTest {

    private static final System.Logger LOG = System.getLogger(DeployOnVirtualServersTest.class.getName());

    private static final String APP_NAME = DeployOnVirtualServersTest.class.getSimpleName() + "App";

    private static final Asadmin ASADMIN = getAsadmin();

    @TempDir
    private static File tempDir;
    private static File warFile;

    @BeforeAll
    public static void prepareDeployment() {
        // Create virtual servers
        assertThat(ASADMIN.exec("create-virtual-server", "--hosts", "localhost", "vserver1"), asadminOK());
        assertThat(ASADMIN.exec("create-virtual-server", "--hosts", "localhost", "vserver2"), asadminOK());

        WebArchive webArchive = ShrinkWrap.create(WebArchive.class)
            .addClass(SimpleResource.class)
            .addClass(SimpleApplication.class)
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        LOG.log(INFO, webArchive.toString(true));

        warFile = new File(tempDir, APP_NAME + ".war");

        webArchive.as(ZipExporter.class).exportTo(warFile, true);
    }

    @AfterAll
    public static void deleteVirtualServers() {
        // Delete virtual servers
        ASADMIN.exec("delete-virtual-server", "vserver1");
        ASADMIN.exec("delete-virtual-server", "vserver2");
    }

    @AfterEach
    public void undeploy() {
        assertThat(ASADMIN.exec("undeploy", APP_NAME), asadminOK());
    }

    @Test
    public void testDefaultDeploy() {
        // By default deploy on all virtual servers except __asadmin
        assertThat(ASADMIN.exec("deploy", warFile.getAbsolutePath()), asadminOK());
    }

    @Test
    public void testDeployWithExplicitVirtualServersList() {
        assertThat(ASADMIN.exec("deploy", "--virtualservers", "server,vserver1", warFile.getAbsolutePath()), asadminOK());
    }
}
