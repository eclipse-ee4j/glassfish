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

import org.glassfish.api.admin.RuntimeType;

import static org.glassfish.api.admin.RuntimeType.DAS;
import static org.glassfish.api.admin.RuntimeType.EMBEDDED;
import static org.glassfish.api.admin.RuntimeType.INSTANCE;

/**
 * @author bnevins
 */
public class GFLauncherFactory {

    /**
     * @param type The type of server to launch.
     * @return A launcher instance that can be used for launching the specified server type.
     * @throws com.sun.enterprise.admin.launcher.GFLauncherException
     */
    public static GFLauncher getInstance(RuntimeType type) throws GFLauncherException {
        switch (type) {
            case DAS:
                return new GlassFishMainLauncher(new GFLauncherInfo(DAS));
            case EMBEDDED:
                return new GFEmbeddedLauncher(new GFLauncherInfo(EMBEDDED));
            case INSTANCE:
                return new GlassFishMainLauncher(new GFLauncherInfo(INSTANCE));
            default:
                throw new GFLauncherException("Only domain, instance and embedded launching are currently supported.");
        }
    }
}
