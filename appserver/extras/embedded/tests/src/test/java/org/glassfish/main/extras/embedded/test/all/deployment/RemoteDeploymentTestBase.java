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

package org.glassfish.main.extras.embedded.test.all.deployment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.glassfish.embeddable.BootstrapProperties;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.glassfish.main.extras.embedded.test.app.ejb.RemoteInterface;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import static java.lang.String.format;
import static org.glassfish.tests.utils.ServerUtils.getFreePort;
import static org.glassfish.tests.utils.ServerUtils.runCommand;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class RemoteDeploymentTestBase {

    private static final String LOOKUP_STRING =
        "java:global/%s/RemoteBean!org.glassfish.main.extras.embedded.test.app.ejb.RemoteInterface";

    protected GlassFish glassfish;

    @BeforeEach
    public void startup() throws GlassFishException {
        GlassFishRuntime glassfishRuntime = GlassFishRuntime.bootstrap(new BootstrapProperties());
        glassfish = glassfishRuntime.newGlassFish(new GlassFishProperties());
        assertThat(glassfish.getStatus(), equalTo(GlassFish.Status.INIT));

        glassfish.start();
        while (glassfish.getStatus() == GlassFish.Status.STARTING) {
            Thread.yield();
        }
        assertThat(glassfish.getStatus(), equalTo(GlassFish.Status.STARTED));

        runCommand(
            glassfish,
            "set",
            "configs.config.server-config.iiop-service.iiop-listener.orb-listener-1.port=" + getFreePort());
        runCommand(
            glassfish,
            "set",
            "configs.config.server-config.iiop-service.iiop-listener.SSL.port=" + getFreePort());
        runCommand(
            glassfish,
            "set",
            "configs.config.server-config.iiop-service.iiop-listener.SSL_MUTUALAUTH.port=" + getFreePort());
    }

    @AfterEach
    public void shutdown() throws GlassFishException {
        glassfish.stop();
        while (glassfish.getStatus() == GlassFish.Status.STOPPING) {
            Thread.yield();
        }
        assertThat(glassfish.getStatus(), equalTo(GlassFish.Status.STOPPED));

        glassfish.dispose();
        assertThat(glassfish.getStatus(), equalTo(GlassFish.Status.DISPOSED));
    }

    protected File createFileFor(Archive<?> archive, String appName) throws IOException {
        File tempDir = Files.createTempDirectory(appName).toFile();
        File appFile = new File(tempDir, appName + extensionFor(archive));
        archive.as(ZipExporter.class).exportTo(appFile, true);
        tempDir.deleteOnExit();
        return appFile;
    }

    private String extensionFor(Archive<?> archive) {
        String extension = ".jar";
        if (archive instanceof WebArchive) {
            extension = ".war";
        } else if (archive instanceof EnterpriseArchive) {
            extension = ".ear";
        }
        return extension;
    }

    protected String getResult(final String appName) throws NamingException {
        InitialContext initialContext = new InitialContext();
        RemoteInterface remoteBean = (RemoteInterface) initialContext.lookup(getLookupString(appName));
        return remoteBean.getMessage();
    }

    protected String getResult(final String appName, final String moduleName) throws NamingException {
        InitialContext initialContext = new InitialContext();
        RemoteInterface remoteBean = (RemoteInterface) initialContext.lookup(getLookupString(appName, moduleName));
        return remoteBean.getMessage();
    }

    private String getLookupString(final String appName) {
        return format(LOOKUP_STRING, appName);
    }

    private String getLookupString(final String appName, final String moduleName) {
        return format(LOOKUP_STRING, appName + "/" + moduleName);
    }
}
