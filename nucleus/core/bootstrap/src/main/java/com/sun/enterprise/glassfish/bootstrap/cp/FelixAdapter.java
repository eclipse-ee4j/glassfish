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

package com.sun.enterprise.glassfish.bootstrap.cp;

import com.sun.enterprise.glassfish.bootstrap.cfg.StartupContextCfg;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class FelixAdapter implements OsgiPlatformAdapter {

    private static final String FELIX_HOME = "FELIX_HOME";
    private final File felixHome;

    public FelixAdapter(StartupContextCfg cfg) {
        this.felixHome = cfg.getOsgiHome(FELIX_HOME, FELIX_HOME, Path.of("osgi", "felix"));
    }


    @Override
    public void addFrameworkJars(ClassPathBuilder cpb) throws IOException {
        cpb.addJar(new File(felixHome, "bin/felix.jar"));
    }
}
