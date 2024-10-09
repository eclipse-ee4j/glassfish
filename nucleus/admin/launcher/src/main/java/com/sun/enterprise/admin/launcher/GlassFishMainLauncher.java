/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

import com.sun.enterprise.universal.io.SmartFile;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.sun.enterprise.util.SystemPropertyConstants.INSTALL_ROOT_PROPERTY;

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
    void internalLaunch() throws GFLauncherException {
        try {
            launchInstance();
        } catch (GFLauncherException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new GFLauncherException(ex);
        }
    }

    @Override
    List<File> getMainClasspath() throws GFLauncherException {
        Path dir = Path.of(getEnvProps().get(INSTALL_ROOT_PROPERTY), "lib", "bootstrap");
        List<File> classpath = Stream
            .of("glassfish.jar", "glassfish-jul-extension.jar", "glassfish-jdk-extensions.jar",
                "simple-glassfish-api.jar")
            .map(dir::resolve).map(Path::toFile).map(SmartFile::sanitize).collect(Collectors.toList());
        if (isFakeLaunch()) {
            return classpath;
        }
        List<File> missing = new ArrayList<>(classpath.size());
        for (File file : classpath) {
            if (!file.exists()) {
                missing.add(file);
            }
        }
        if (missing.isEmpty()) {
            return classpath;
        }
        throw new GFLauncherException("nobootjar", missing);
    }

    @Override
    String getMainClass() throws GFLauncherException {
        return MAIN_CLASS;
    }
}
