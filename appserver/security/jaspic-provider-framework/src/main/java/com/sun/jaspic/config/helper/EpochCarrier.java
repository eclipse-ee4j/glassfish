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

package com.sun.jaspic.config.helper;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author Ron Monzillo
 */

public class EpochCarrier {

    private Lock instanceReadLock;
    private Lock instanceWriteLock;

    private long epoch;

    public EpochCarrier() {
        ReentrantReadWriteLock instanceReadWriteLock = new ReentrantReadWriteLock();
        instanceReadLock = instanceReadWriteLock.readLock();
        instanceWriteLock = instanceReadWriteLock.writeLock();
        epoch = 0L;
    }

    public long increment() {
        instanceWriteLock.lock();
        long before;
        try {
            before = epoch;
            epoch = epoch + 1;
        } finally {
            instanceWriteLock.unlock();
        }
        return before;
    }

    public long getEpoch() {
        instanceReadLock.lock();
        try {
            return epoch;
        } finally {
            instanceReadLock.unlock();
        }
    }

    public boolean hasChanged(long reference) {
        instanceReadLock.lock();
        try {
            return epoch != reference;
        } finally {
            instanceReadLock.unlock();
        }
    }

}
