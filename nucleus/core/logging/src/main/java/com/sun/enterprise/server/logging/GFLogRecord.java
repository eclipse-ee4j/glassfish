/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.server.logging;

import java.util.logging.LogRecord;
import java.util.logging.Level;

/**
 * This class provides additional attributes not supported by JUL LogRecord
 * @author rinamdar
 */
public class GFLogRecord extends LogRecord {

    /**
     * SVUID for serialization compatibility
     */
    private static final long serialVersionUID = -818792012235891720L;

    private String threadName;

    public GFLogRecord(Level level, String msg) {
        super(level, msg);
    }

    public GFLogRecord(LogRecord record) {
        this(record.getLevel(), record.getMessage());

        this.setLoggerName(record.getLoggerName());
        this.setMillis(record.getMillis());
        this.setParameters(record.getParameters());
        this.setResourceBundle(record.getResourceBundle());
        this.setResourceBundleName(record.getResourceBundleName());
        this.setSequenceNumber(record.getSequenceNumber());
        this.setSourceClassName(record.getSourceClassName());
        this.setSourceMethodName(record.getSourceMethodName());
        this.setThreadID(record.getThreadID());
        this.setThrown(record.getThrown());
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }
}
