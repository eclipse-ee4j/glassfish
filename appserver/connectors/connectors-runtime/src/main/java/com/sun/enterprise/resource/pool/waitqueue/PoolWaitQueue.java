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

package com.sun.enterprise.resource.pool.waitqueue;

import java.util.Collection;

/**
 * Represents the pool wait queue<br>
 * To plug-in multiple implementation of wait-queue<br>
 *
 * @author Jagadish Ramu
 */
public interface PoolWaitQueue {
    String DEFAULT_WAIT_QUEUE = "DEFAULT_WAIT_QUEUE";
    String THREAD_PRIORITY_BASED_WAIT_QUEUE = "THREAD_PRIORITY_BASED_WAIT_QUEUE";

    /**
     * returns the length of wait queue
     * @return length of wait queue.
     */
    int getQueueLength();

    /**
     * resource requesting thread will be added to queue<br>
     * and the object on which it is made to wait is returned
     * @param o Object
     */
    void addToQueue(Object o);

    /**
     * removes the specified object (resource request) from the queue
     * @param o Object
     * @return boolean indicating whether the object was removed or not
     */
    boolean removeFromQueue(Object o);

    /**
     * removes the first object (resource request) from the queue
     */
    /*
    Object removeFirst();
    */

    /**
     * removes the first object (resource request) from the queue
     * @return Object first object
     */
    Object remove();

    /**
     * returns (does not remove) the first object (resource request) from the queue
     * @return Object first object
     */
    Object peek();

    /**
     * used to get access to the list of waiting clients<br>
     * Useful in case of rolling over from one pool to another
     * eg: transparent-dynamic-pool-reconfiguration.
     * @return Collection
     */
    Collection getQueueContents();
}
