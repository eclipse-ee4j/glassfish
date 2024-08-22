/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

import com.sun.appserv.connectors.internal.api.PoolingException;
import com.sun.logging.LogDomains;

import java.util.logging.Logger;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.WARNING;

/**
 * Factory to create appropriate Pool Wait Queue
 *
 * @author Jagadish Ramu
 */
public class PoolWaitQueueFactory {

    private final static Logger _logger = LogDomains.getLogger(PoolWaitQueueFactory.class, LogDomains.RSR_LOGGER);

    public static PoolWaitQueue createPoolWaitQueue(String className) throws PoolingException {
        PoolWaitQueue waitQueue;

        if (className != null) {
            waitQueue = initializeCustomWaitQueueInPrivilegedMode(className);
        } else {
            waitQueue = new DefaultPoolWaitQueue();
            debug("Initializing Default Pool Wait Queue");
        }
        return waitQueue;
    }

    private static PoolWaitQueue initializeCustomWaitQueueInPrivilegedMode(final String className) throws PoolingException {
        Object result = null;

        try {
            result = initializeCustomWaitQueue(className);
        } catch (Exception e) {
            _logger.log(WARNING, "pool.waitqueue.init.failure", className);
            _logger.log(WARNING, "pool.waitqueue.init.failure.exception", e);
        }

        if (result == null) {
            throw new PoolingException("Unable to initalize custom PoolWaitQueue : " + className);
        }

        return (PoolWaitQueue) result;
    }

    private static PoolWaitQueue initializeCustomWaitQueue(String className) throws Exception {
        PoolWaitQueue waitQueue;
        Class class1 = Thread.currentThread().getContextClassLoader().loadClass(className);
        waitQueue = (PoolWaitQueue) class1.newInstance();

        _logger.log(FINEST, "Using Pool Wait Queue class : ", className);
        return waitQueue;
    }

    private static void debug(String debugStatement) {
        _logger.log(FINE, debugStatement);
    }
}
