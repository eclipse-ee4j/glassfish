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

import com.sun.enterprise.glassfish.bootstrap.cfg.GFBootstrapProperties;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.osgi.framework.Constants.FRAMEWORK_STORAGE;

public class EquinoxAdapter extends OsgiPlatformAdapter {

    private static final String EQUINOX_HOME = "EQUINOX_HOME";

    private final File equinoxHome;

    public EquinoxAdapter(GFBootstrapProperties properties) {
        super(properties);
        this.equinoxHome = properties.getOsgiHome(FRAMEWORK_STORAGE, FRAMEWORK_STORAGE, Path.of("osgi", "equinox"));
    }

    @Override
    public void addFrameworkJars(ClassPathBuilder builder) throws IOException {
        builder.addJarFolder(equinoxHome);
    }

    @Override
    protected String getFrameworkStorageDirectoryName() {
        return "equinox";
    }
}
