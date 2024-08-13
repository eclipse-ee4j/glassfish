/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.gjc.util;

import com.sun.logging.LogDomains;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.jdbc.SQLTraceListener;
import org.glassfish.api.jdbc.SQLTraceRecord;

/**
 * Implementation of SQLTraceListener to listen to events related to a sql
 * record tracing.
 *
 * @author Shalini M
 */
public class SQLTraceLogger implements SQLTraceListener {

    private static Logger _logger = initLogger();

    private static Logger initLogger() {
        _logger = LogDomains.getLogger(SQLTraceLogger.class, LogDomains.SQL_TRACE_LOGGER);
        return _logger;
    }

    public SQLTraceLogger() {

    }

    public void sqlTrace(SQLTraceRecord record) {
        _logger.log(Level.FINE, record.toString());
    }

}
