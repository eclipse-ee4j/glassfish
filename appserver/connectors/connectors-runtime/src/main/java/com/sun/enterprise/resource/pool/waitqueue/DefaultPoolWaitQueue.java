/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.resource.pool.waitqueue;

import com.sun.logging.LogDomains;

import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default wait queue for the connection pool
 *
 * @author Jagadish Ramu
 */
public class DefaultPoolWaitQueue implements PoolWaitQueue {

    private LinkedList list;
    protected final static Logger _logger = LogDomains.getLogger(DefaultPoolWaitQueue.class, LogDomains.RSR_LOGGER);

    private void initializeDefaultQueue() {
        list = new LinkedList();
        debug("Initializing default Pool Wait Queue");
    }

    public DefaultPoolWaitQueue() {
        initializeDefaultQueue();
    }

    @Override
    public synchronized int getQueueLength() {
        return list.size();
    }

    @Override
    public synchronized void addToQueue(Object waitMonitor) {

        list.addLast(waitMonitor);
    }

    @Override
    public synchronized boolean removeFromQueue(Object o) {
        return list.remove(o);
    }

    /*
     * public synchronized Object removeFirst() { return list.removeFirst(); }
     */

    @Override
    public synchronized Object remove() {
        return list.removeFirst();
    }

    @Override
    public Object peek() {
        Object result = null;
        if (list.size() > 0) {
            result = list.get(0);
        }
        return result;
    }

    @Override
    public Collection getQueueContents() {
        return list;
    }

    protected void debug(String debugStatement) {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, debugStatement);
        }
    }
}
