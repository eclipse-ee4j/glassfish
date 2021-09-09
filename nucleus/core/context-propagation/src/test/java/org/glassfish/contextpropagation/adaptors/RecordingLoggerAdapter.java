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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RecordingLoggerAdapter implements LoggerAdapter {

    Level lastLevel;
    MessageID lastMessageID;
    Object[] lastArgs;
    Throwable lastThrowable;

    @Override
    public boolean isLoggable(Level level) {
        return true;
    }


    @Override
    public void log(Level level, MessageID messageID, Object... args) {
        lastLevel = level;
        lastMessageID = messageID;
        lastArgs = args;
        lastThrowable = null;
    }


    @Override
    public void log(Level level, Throwable t, MessageID messageID, Object... args) {
        lastLevel = level;
        lastMessageID = messageID;
        lastArgs = args;
        lastThrowable = t;
    }


    public void verify(Level level, Throwable t, MessageID messageID, Object... args) {
        assertEquals(lastLevel, level);
        assertEquals(lastThrowable, t);
        assertEquals(lastMessageID, messageID);
        assertArrayEquals(lastArgs, args);
        lastLevel = null;
        lastThrowable = null;
        lastMessageID = null;
        lastArgs = null;
    }

}
