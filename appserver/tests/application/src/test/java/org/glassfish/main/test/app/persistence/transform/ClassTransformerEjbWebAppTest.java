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
import java.nio.file.Files;

import org.glassfish.main.test.app.persistence.transform.ejb.ClassTransformerBean;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;

import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.WARNING;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests bytecode preprocessing in ASURLClassLoader.
 */
public class ClassTransformerEjbWebAppTest extends ClassTransformerTestBase {

    private static final System.Logger LOG = System.getLogger(ClassTransformerEjbWebAppTest.class.getName());

    private static final String APP_NAME = "TransformEjbWebApp";

    @Test
    public void testDeploy() throws IOException {
        File warFile = createDeployment();
        try {
            assertThat(ASADMIN.exec("deploy", warFile.getAbsolutePath()), asadminOK());
            assertThat(ASADMIN.exec("undeploy", APP_NAME), asadminOK());
        } finally {
            try {
                Files.deleteIfExists(warFile.toPath());
            } catch (IOException e){
                LOG.log(WARNING, "An error occurred while remove temp file " + warFile.getAbsolutePath(), e);
            }
        }
    }

    private File createDeployment() throws IOException {
        WebArchive webArchive = ShrinkWrap.create(WebArchive.class)
            .addAsLibrary(createProvider())
            .addClass(ClassTransformerBean.class)
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
            .addAsResource(
                        getTestResource("persistence.xml"),
                "META-INF/persistence.xml");

        LOG.log(INFO, webArchive.toString(true));

        return createDeploymentFile(webArchive, APP_NAME);
    }
}
