/*
 * Copyright (c) 2023 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.resource.pool;

import com.sun.appserv.connectors.internal.api.PoolingException;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * Atomic updates of pool size.
 * <p>
 * The current count cannot exceed the pool capacity and cannot be less than 0.
 * Exceeding the capacity value means throwing an exception.
 * Exceeding 0 doesn't cause any error.
 *
 * @author David Matejcek
 */
public final class PoolSize {

    private final AtomicInteger currentCount;
    private final int capacity;

    /**
     * @param capacity max pool size
     */
    public PoolSize(int capacity) {
        this.currentCount = new AtomicInteger(0);
        this.capacity = capacity;
    }


    /**
     * @return maximal possible count of provided resources at the same time.
     */
    public int getCapacity() {
        return this.capacity;
    }


    /**
     * @return actual count of provided resources
     */
    public int getCurrentCount() {
        return this.currentCount.get();
    }


    /**
     * Increases the count of provided resources.
     *
     * @throws PoolingException if the current count already reached the capacity.
     */
    public void increment() throws PoolingException {
        if (this.currentCount.getAndUpdate(v -> Math.min(v + 1, this.capacity)) == this.capacity) {
            throw new PoolingException("Count of provided connections is already equal to the capacity (" + capacity
                + ") therefore you cannot allocate any more resources.");
        }
    }


    /**
     * Decreases the count of provided resources.
     * If is already zero, doesn't do anything.
     */
    public void decrement() {
        this.currentCount.getAndUpdate(v -> Math.max(v - 1, 0));
    }


    /**
     * Returns the {@link #getCurrentCount()}.
     */
    @Override
    public String toString() {
        return this.currentCount.toString();
    }
}
