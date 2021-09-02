/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package org.glassfish.contextpropagation.adaptors;

import org.glassfish.contextpropagation.bootstrap.LoggerAdapter;
import org.junit.jupiter.api.Test;

public class MockLoggerAdapter implements LoggerAdapter {

    // TODO TIP: Change the Level constant to control what is logged, use null to reduce output to a minimum
    static final Level LOGGING_LEVEL = null; // Level.WARN;

    @Override
    public boolean isLoggable(Level level) {
        return _isLoggable(level);
    }


    @Override
    public void log(Level level, MessageID messageID, Object... args) {
        System.out.println(format(messageID.defaultMessage, args));

    }


    private String format(String defaultMessage, Object... args) {
        // $1 refers to the group %1 is equivalent to %1$s
        String formatString = defaultMessage.replaceAll("%([0-9]*)", "%$1\\$s");
        return String.format(formatString, args);
    }


    @Override
    public void log(Level level, Throwable t, MessageID messageID, Object... args) {
        log(level, messageID, args);
        t.printStackTrace();
    }


    @Test
    public void testFormat() {
        debug(format("arg 1:%1, arg2: %2", "one", "two"));
    }


    private static boolean _isLoggable(Level level) {
        return LOGGING_LEVEL != null && level.ordinal() <= LOGGING_LEVEL.ordinal();
    }


    public static void debug(String s) {
        if (_isLoggable(Level.DEBUG)) {
            System.out.println(s);
        }
    }

}
