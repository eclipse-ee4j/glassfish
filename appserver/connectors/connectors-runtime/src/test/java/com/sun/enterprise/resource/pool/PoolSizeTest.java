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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author David Matejcek
 */
public class PoolSizeTest {

    @Test
    void increaseAndDecrease() throws Exception {
        PoolSize poolSize = new PoolSize(2);
        assertEquals(2, poolSize.getCapacity());
        poolSize.increment();
        assertEquals(1, poolSize.getCurrentCount());
        poolSize.increment();
        assertEquals(2, poolSize.getCurrentCount());
        PoolingException e = assertThrows(PoolingException.class, () -> poolSize.increment());
        assertAll(
            () -> assertEquals(2, poolSize.getCurrentCount()),
            () -> assertEquals("Count of provided connections is already equal to the capacity (2) therefore"
                + " you cannot allocate any more resources.", e.getMessage())
        );
        poolSize.decrement();
        assertEquals(1, poolSize.getCurrentCount());
        poolSize.decrement();
        assertEquals(0, poolSize.getCurrentCount());
        poolSize.decrement();
        assertEquals(0, poolSize.getCurrentCount());
    }


    @Test
    void threadSafety() throws Exception {
        AtomicInteger increases = new AtomicInteger();
        AtomicInteger decreases = new AtomicInteger();
        PoolSize poolSize = new PoolSize(100);
        ExecutorService threadPool = Executors.newFixedThreadPool(500);
        int taskCount = 10000;
        List<Callable<Void>> tasks = new ArrayList<>(taskCount);
        for (int i = 0; i < taskCount; i++) {
            tasks.add(() -> {
                poolSize.increment();
                // increment succeeded
                increases.incrementAndGet();
                Thread.sleep(0, 10);
                poolSize.decrement();
                // if inc succeeded, dec must succeed too -> same count.
                decreases.incrementAndGet();
                return null;
            });
        }
        List<Future<Void>> futures = threadPool.invokeAll(tasks, 10, TimeUnit.SECONDS);
        assertAll(
            () -> assertTrue(futures.stream().allMatch(f -> f.isDone()), "All done."),
            () -> assertFalse(futures.stream().anyMatch(f -> f.isCancelled()), "None cancelled."),
            () -> assertThat("Count of increases", increases.get(), greaterThan(poolSize.getCapacity())),
            () -> assertEquals(increases.get(), decreases.get(), "Count of increases and decreases"),
            () -> assertEquals(0, poolSize.getCurrentCount(), "Current count")
        );
    }
}
