/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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
import java.util.ArrayList;
import java.util.List;

import static com.sun.enterprise.util.SystemPropertyConstants.INSTALL_ROOT_PROPERTY;

/**
 *
 * @author bnevins
 */
class GFInstanceLauncher extends GFLauncher {

    private static final String MAIN_CLASS = "com.sun.enterprise.glassfish.bootstrap.GlassFishMain";
    private static final String BOOTSTRAP_JAR = "glassfish.jar";

    GFInstanceLauncher(GFLauncherInfo info) {
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
        List<File> list = new ArrayList<>();
        File dir = new File(getEnvProps().get(INSTALL_ROOT_PROPERTY), "modules");

        File bootjar = new File(dir, BOOTSTRAP_JAR);
        if (!bootjar.exists() && !isFakeLaunch()) {
            throw new GFLauncherException("nobootjar", dir.getPath());
        }

        if (bootjar.exists()) {
            list.add(SmartFile.sanitize(bootjar));
        }

        return list;
    }

    @Override
    String getMainClass() throws GFLauncherException {
        return MAIN_CLASS;
    }

}
