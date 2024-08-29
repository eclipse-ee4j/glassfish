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

package com.sun.enterprise.glassfish.bootstrap;

import com.sun.enterprise.glassfish.bootstrap.osgi.impl.ClassPathBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.INSTALL_ROOT_PROP_NAME;
import static com.sun.enterprise.glassfish.bootstrap.log.LogFacade.BOOTSTRAP_LOGGER;
import static java.util.logging.Level.FINE;

class ClassLoaderBuilder {

    private final ClassPathBuilder cpb;
    private final Path glassfishDir;
    private final Properties ctx;

    ClassLoaderBuilder(Properties ctx) throws IOException {
        this.ctx = ctx;
        cpb = new ClassPathBuilder();
        glassfishDir = getInstallRoot(ctx);
    }

    void addPlatformDependencies() throws IOException {
        OsgiPlatformFactory.getOsgiPlatformAdapter(ctx).addFrameworkJars(cpb);
    }

    /**
     * Adds JDK tools.jar to classpath.
     */
    void addJDKToolsJar() {
        File jdkToolsJar = Util.getJDKToolsJar();
        try {
            cpb.addJar(jdkToolsJar);
        } catch (IOException ioe) {
            // on the mac, it happens all the time
            BOOTSTRAP_LOGGER.log(FINE, "JDK tools.jar does not exist at {0}", jdkToolsJar);
        }
    }

    public ClassLoader build(ClassLoader delegate) {
        return cpb.create(delegate);
    }

    public void addLauncherDependencies() throws IOException {
        cpb.addJar(glassfishDir.resolve(Path.of("modules", "glassfish.jar")).toFile());
    }

    public void addServerBootstrapDependencies() throws IOException {
        cpb.addJar(glassfishDir.resolve(Path.of("modules", "simple-glassfish-api.jar")).toFile());
        cpb.addJar(glassfishDir.resolve(Path.of("lib", "bootstrap", "glassfish-jul-extension.jar")).toFile());
    }


    private static Path getInstallRoot(Properties context) throws IOException {
        String property = context.getProperty(INSTALL_ROOT_PROP_NAME);
        if (property.indexOf(0) >= 0) {
            throw new IOException("The property " + INSTALL_ROOT_PROP_NAME + " contains a null byte!");
        }
        final File file = new File(property);
        return file.getCanonicalFile().toPath();
    }
}