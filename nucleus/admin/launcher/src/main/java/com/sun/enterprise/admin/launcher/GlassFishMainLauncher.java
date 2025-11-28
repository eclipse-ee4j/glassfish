/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.launcher;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.glassfish.embeddable.GlassFishVariable.INSTALL_ROOT;

/**
 * Prepares JVM configuration to use GlassFishMain to launch the domain or instance of the server
 * and launches it.
 *
 * @author bnevins
 */
class GlassFishMainLauncher extends GFLauncher {

    private static final String MAIN_CLASS = "com.sun.enterprise.glassfish.bootstrap.GlassFishMain";

    // sample profiler config
    //
    // <java-config classpath-suffix="" debug-enabled="false" debug-options="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=9009" env-classpath-ignored="true" java-home="${com.sun.aas.javaRoot}" javac-options="-g" rmic-options="-iiop -poa -alwaysgenerate -keepgenerated -g" system-classpath="">
    //   <profiler classpath="c:/dev/elf/dist/elf.jar" enabled="false" name="MyProfiler" native-library-path="c:/bin">
    //     <jvm-options>-Dprofiler3=foo3</jvm-options>
    //     <jvm-options>-Dprofiler2=foo2</jvm-options>
    //     <jvm-options>-Dprofiler1=foof</jvm-options>
    //   </profiler>

    GlassFishMainLauncher(GFLauncherInfo info) {
        super(info);
    }

    @Override
    List<File> getMainClasspath() throws GFLauncherException {
        return List.of();
    }

    @Override
    List<File> getMainModulepath() throws GFLauncherException {
        Path installRoot = new File(getEnvProps().get(INSTALL_ROOT.getPropertyName())).toPath();
        return List.of(installRoot.resolve(Path.of("lib", "bootstrap")).toAbsolutePath().normalize().toFile());
    }

    @Override
    String getMainClass() throws GFLauncherException {
        return MAIN_CLASS;
    }
}
