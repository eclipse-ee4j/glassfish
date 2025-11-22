/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.universal.xml.MiniXmlParserException;

import org.glassfish.main.jul.GlassFishLogManagerInitializer;

import static com.sun.enterprise.admin.launcher.GFLauncherLogger.LAUNCH_FAILURE;
import static org.glassfish.api.admin.RuntimeType.DAS;

/**
 * @author bnevins
 */
public class GFLauncherMain {

    static {
        // The GlassFishLogManager must be set before the first usage of any JUL component,
        // it cannot be done later.
        if (!GlassFishLogManagerInitializer.tryToSetAsDefault()) {
            throw new IllegalStateException("GlassFishLogManager is not set as the default LogManager!");
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            GFLauncher launcher = GFLauncherFactory.getInstance(DAS);
            launcher.getInfo().addArgs(args);
            launcher.setup();
            launcher.launch();
        } catch (GFLauncherException | MiniXmlParserException ex) {
            GFLauncherLogger.severe(LAUNCH_FAILURE, ex);
        }
    }
}
