/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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
 */package org.glassfish.main.test.app.connpool.lib;

import java.util.logging.Logger;

import org.glassfish.api.jdbc.SQLTraceListener;
import org.glassfish.api.jdbc.SQLTraceRecord;

public class LastTraceSQLTraceListener implements SQLTraceListener {

    private static final Logger logger = Logger.getLogger(LastTraceSQLTraceListener.class.getName());

    public static SQLTraceRecord lastTraceRecord;

    @Override
    public void sqlTrace(SQLTraceRecord sqltr) {
        logger.fine(() -> "Trace record: " + sqltr);
        if (sqltr.getSqlQuery().isPresent()) {
            lastTraceRecord = sqltr;
        }
        sqltr.getCallingApplicationMethod().ifPresent(caller -> {
            logger.fine(() -> "Method calling SQL: " + caller);
        });
    }

}
