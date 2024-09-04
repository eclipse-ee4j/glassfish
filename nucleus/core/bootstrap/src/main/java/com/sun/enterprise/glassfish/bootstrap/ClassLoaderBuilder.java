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

import com.sun.enterprise.glassfish.bootstrap.cfg.GFBootstrapProperties;
import com.sun.enterprise.glassfish.bootstrap.osgi.impl.ClassPathBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static com.sun.enterprise.glassfish.bootstrap.log.LogFacade.BOOTSTRAP_LOGGER;
import static java.util.logging.Level.FINE;

class ClassLoaderBuilder {

    private final ClassPathBuilder cpBuilder;
    private final GFBootstrapProperties ctx;

    ClassLoaderBuilder(GFBootstrapProperties ctx) {
        this.ctx = ctx;
        this.cpBuilder = new ClassPathBuilder();
    }

    void addPlatformDependencies() throws IOException {
        OsgiPlatformFactory.getOsgiPlatformAdapter(ctx).addFrameworkJars(cpBuilder);
    }

    /**
     * Adds JDK tools.jar to classpath.
     */
    void addJDKToolsJar() {
        File jdkToolsJar = Util.getJDKToolsJar();
        try {
            cpBuilder.addJar(jdkToolsJar);
        } catch (IOException ioe) {
            // on the mac, it happens all the time
            BOOTSTRAP_LOGGER.log(FINE, "JDK tools.jar does not exist at {0}", jdkToolsJar);
        }
    }

    ClassLoader build(ClassLoader delegate) {
        return cpBuilder.create(delegate);
    }

    void addLauncherDependencies() throws IOException {
        cpBuilder.addJar(ctx.getFileUnderInstallRoot(Path.of("modules", "glassfish.jar")));
    }

    void addServerBootstrapDependencies() throws IOException {
        cpBuilder.addJar(ctx.getFileUnderInstallRoot(Path.of("modules", "simple-glassfish-api.jar")));
        cpBuilder.addJar(ctx.getFileUnderInstallRoot(Path.of("lib", "bootstrap", "glassfish-jul-extension.jar")));
    }
}
