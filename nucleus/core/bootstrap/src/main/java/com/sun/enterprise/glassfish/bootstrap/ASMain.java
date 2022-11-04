/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tag Main to get the manifest file
 */
public class ASMain {

    private static final Logger LOG = Logger.getLogger(ASMain.class.getName());

    /**
     * Most of the code in this file has been moved to MainHelper
     * and ASMainOSGi
     */
    public static void main(final String[] args) throws Exception {
        try {
            GlassFishMain.main(args);
        } catch (Throwable t) {
            LOG.log(Level.SEVERE, "Error starting GlassFish", t);
            throw t;
        }
    }
}
