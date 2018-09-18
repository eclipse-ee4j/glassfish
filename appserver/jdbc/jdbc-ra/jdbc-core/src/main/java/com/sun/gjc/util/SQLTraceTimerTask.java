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

package com.sun.gjc.util;

import java.util.TimerTask;

/**
 * Sql Tracing Timer task used to preform a purgeEntries of the cache of objects
 * maintained by the sql tracing mechanism.
 *
 * @author Shalini M
 */
public class SQLTraceTimerTask extends TimerTask {

    private SQLTraceCache cache;

    SQLTraceTimerTask(SQLTraceCache cache) {
        this.cache = cache;
    }

    /**
     * Sql Tracing timer task to clean up the sql trace cache
     */
    @Override
    public void run() {
        //Redirecting the purge operation of the cache to the cache factory
        cache.purgeEntries();
    }

}
