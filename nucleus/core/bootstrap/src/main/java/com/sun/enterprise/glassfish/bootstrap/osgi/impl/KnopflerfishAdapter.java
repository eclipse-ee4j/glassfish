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

package com.sun.enterprise.glassfish.bootstrap.osgi.impl;

import com.sun.enterprise.glassfish.bootstrap.cfg.StartupContextCfg;
import com.sun.enterprise.glassfish.bootstrap.cp.ClassPathBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class KnopflerfishAdapter implements OsgiPlatformAdapter {

    private static final String KF_HOME = "KNOPFLERFISH_HOME";
    private final File kfHome;

    public KnopflerfishAdapter(StartupContextCfg cfg) {
        this.kfHome = cfg.getOsgiHome(KF_HOME, KF_HOME, Path.of("osgi", "knopflerfish.org", "osgi"));
    }


    @Override
    public void addFrameworkJars(ClassPathBuilder cpb) throws IOException {
        cpb.addJar(new File(kfHome, "framework.jar"));
    }
}
