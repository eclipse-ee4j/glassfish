/*
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

package com.sun.enterprise.util;

import com.sun.enterprise.universal.i18n.LocalStringsImpl;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bnevins
 */
public class EarlyLogger {
    private EarlyLogger() {
        // no instances allowed...
    }

    public static void add(Level level, String message) {
        messages.add(new LevelAndMessage(level, prepend + message));
        // also log to the console...
        logger.log(level, message);
    }

    public static List<LevelAndMessage> getEarlyMessages() {
        return messages;
    }

    public final static class LevelAndMessage {

        private final String msg;
        private final Level level;

        LevelAndMessage(Level l, String m) {
            msg = m;
            level = l;
        }

        public final String getMessage() {
            return msg;
        }

        public final Level getLevel() {
            return level;
        }
    }
    private final static List<LevelAndMessage> messages =
            new CopyOnWriteArrayList<LevelAndMessage>();
    private final static LocalStringsImpl strings =
            new LocalStringsImpl(EarlyLogger.class);
    private final static String prepend = strings.get("EarlyLogger.prepend");
    private final static Logger logger = Logger.getAnonymousLogger();
}
