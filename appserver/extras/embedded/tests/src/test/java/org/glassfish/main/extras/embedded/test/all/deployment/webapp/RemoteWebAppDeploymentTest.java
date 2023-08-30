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

package org.glassfish.main.extras.embedded.test.all.deployment.webapp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.naming.NamingException;

import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.main.extras.embedded.test.all.deployment.RemoteDeploymentTestBase;
import org.glassfish.main.extras.embedded.test.app.ejb.RemoteBean;
import org.glassfish.main.extras.embedded.test.app.ejb.RemoteInterface;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;

import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.WARNING;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class RemoteWebAppDeploymentTest extends RemoteDeploymentTestBase {

    private static final System.Logger LOG = System.getLogger(RemoteWebAppDeploymentTest.class.getName());

    private static final String APP_NAME = "RemoteWebApp";

    private static final String LIB_FILE_NAME = "RemoteLib.jar";

    @Test
    public void testDeployAndUndeployApplication() throws GlassFishException, IOException, NamingException {
        Deployer deployer = glassfish.getDeployer();
        File warFile = createDeployment();
        try {
            assertThat(deployer.deploy(warFile), equalTo(APP_NAME));
            assertThat(getResult(APP_NAME), equalTo(Runtime.version().toString()));
        } finally {
            try {
                Files.deleteIfExists(warFile.toPath());
            } catch (IOException e){
                LOG.log(WARNING, "An error occurred while remove temp file " + warFile.getAbsolutePath(), e);
            }
            deployer.undeploy(APP_NAME);
        }
    }

    private File createDeployment() throws IOException {
        JavaArchive javaArchive = ShrinkWrap.create(JavaArchive.class, LIB_FILE_NAME)
            .addClass(RemoteInterface.class);

        WebArchive webArchive = ShrinkWrap.create(WebArchive.class)
            .addClass(RemoteBean.class)
            .addAsLibrary(javaArchive);

        LOG.log(INFO, webArchive.toString(true));

        return createFileFor(webArchive, APP_NAME);
    }
}
