/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.flashlight.impl.client;

import com.sun.enterprise.util.Utility;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.flashlight.FlashlightLoggerInfo;

/**
 * Nice easy simple fast (and therefore more likely to use!) class for logging
 * messages. Has built-in support for local strings. If the string is not found
 * then the infrastructure logger code will look for 'log strings'
 *
 * @author Byron Nevins
 */
final class Log {

    private static final Logger logger;

    static {
        logger = FlashlightLoggerInfo.getLogger();

        if (Boolean.parseBoolean(Utility.getEnvOrProp("AS_DEBUG")))
            logger.setLevel(Level.ALL);
    }

    static Logger getLogger() {
        return logger;
    }

    static void finer(String msg, Object... objs) {
        logger.finer(Strings.get(msg, objs));
    }

    static void fine(String msg, Object... objs) {
        logger.fine(Strings.get(msg, objs));
    }

    static void info(String msg, Object... objs) {
        logger.info(Strings.get(msg, objs));
    }

    static void warning(String msg, Object... objs) {
        logger.warning(Strings.get(msg, objs));
    }

    static void severe(String msg, Object... objs) {
        logger.severe(Strings.get(msg, objs));
    }
}
