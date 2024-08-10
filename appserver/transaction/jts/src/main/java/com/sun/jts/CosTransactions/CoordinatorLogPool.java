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

package com.sun.jts.CosTransactions;

import java.util.Hashtable;
import java.util.Stack;


/**
 * The CoordinatorLogPool is used as a cache for CoordinatorLog objects.
 * This pool allows the re-use of these objects which are very expensive
 * to instantiate.
 *
 * The pool is used by replacing calls to 'new CoordinatorLog()' in the
 * TopCoordinator with calls to CoordinatorLogPool.getCoordinatorLog().
 * The getCoordinatorLog() method attempts to return a CoordinatorLog
 * from the pool. If the pool is empty it instantiates a new
 * CoordinatorLog.
 *
 * Objects are re-used by calling CoordinatorLogPool.putCoordinatorLog()
 * to return a CoordinatorLog object back to the pool. At this time a
 * check is made to ensure that the internal pool size doesn't exceed a
 * pre set limit. If it does, then the object is discarded and not put
 * back into the pool.
 *
 * The pool was added to improve performance of transaction logging
 *
 * @version 1.00
 *
 * @author Arun Krishnan
 *
 * @see
*/
class CoordinatorLogPool {

    private Stack pool;
    private static final int MAXSTACKSIZE = 3;

    public static CoordinatorLogPool CLPool = new CoordinatorLogPool();
    public static Hashtable CLPooltable = new Hashtable();


    /**
     * constructor
     *
     */
    public CoordinatorLogPool() {
        pool = new Stack();
    }

    /**
     * get a CoordinatorLog object from the cache. Instantiate a
     * new CoordinatorLog object if the cache is empty.
     *
     */
    public static synchronized CoordinatorLog getCoordinatorLog() {
        if (Configuration.isDBLoggingEnabled() ||
            Configuration.isFileLoggingDisabled()) {
            return null;
        }
        if (CLPool.pool.empty()) {
            return new CoordinatorLog();
        }
        else {
            CoordinatorLog cl = (CoordinatorLog) CLPool.pool.pop();
            return cl;
        }
    }

    /**
     * return a CoordinatorLog object to the cache. To limit the size of
     * the cache a check is made to ensure that the cache doesn't
     * already have more that MAXSTACKSIZE elements. If so the object
     * being returned is discarded.
     *
     */
    public static void putCoordinatorLog(CoordinatorLog cl) {
        if (CLPool.pool.size() <= MAXSTACKSIZE) {
            CLPool.pool.push(cl);
        }
    }

    // Added to support delegated recovery: multiple logs should coexist
    public static synchronized CoordinatorLog getCoordinatorLog(String logPath) {
        CoordinatorLogPool clpool = (CoordinatorLogPool)CLPooltable.get(logPath);
        if (clpool == null) {
            clpool = new CoordinatorLogPool();
            CLPooltable.put(logPath,clpool);
        }
        if (clpool.pool.empty()) {
            return new CoordinatorLog(logPath);
        }
        else {
            return (CoordinatorLog)clpool.pool.pop();
        }
    }

    // Added to support delegated recovery: multiple logs should coexist
    public static void putCoordinatorLog(CoordinatorLog cl, String logPath) {
        CoordinatorLogPool clpool = (CoordinatorLogPool)CLPooltable.get(logPath);
        if (clpool == null) {
            clpool = new CoordinatorLogPool();
            CLPooltable.put(logPath,clpool);
        }
        if (clpool.pool.size() <= MAXSTACKSIZE) {
            clpool.pool.push(cl);
        }
    }

}

