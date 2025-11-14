/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.test.app.monitoring;

import java.lang.System.Logger;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.glassfish.main.test.app.monitoring.ThreadPoolMonitoringTest.Action;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.TRACE;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpLoadGenerator extends Thread implements AutoCloseable {
    private static final Logger LOG = System.getLogger(HttpLoadGenerator.class.getName());

    private final AtomicInteger threadCounter = new AtomicInteger();
    private final AtomicInteger requestCounter = new AtomicInteger();
    private final int maxParallel;
    private final ExecutorService executor;
    private final Action<?> action;
    private final int maxRequests;


    HttpLoadGenerator(int port, List<UUID> locks, boolean lock) {
        setName("HttpLoadGenerator-" + (lock ? "lock" : "unlock"));
        this.maxParallel = locks.size();
        this.maxRequests = locks.size();
        this.executor = Executors.newFixedThreadPool(maxParallel,
            t -> new Thread(t, "HttpClient-" + threadCounter.incrementAndGet()));
        final ConcurrentLinkedDeque<UUID> lockIds = new ConcurrentLinkedDeque<>(locks);
        final AppClient client = new AppClient(port, 5000);
        this.action = lock ? () -> client.lock(lockIds.remove()) : () -> client.unlock(lockIds.remove());
    }

    HttpLoadGenerator(int port, int maxParallel, int maxRequests) {
        setName("HttpLoadGenerator-test");
        this.maxParallel = maxParallel;
        this.maxRequests = maxRequests;
        this.executor = Executors.newFixedThreadPool(maxParallel,
            t -> new Thread(t, "HttpClient-" + threadCounter.incrementAndGet()));
        final AppClient client = new AppClient(port, 5000);
        this.action = client::test;
    }

    @Override
    public void run() {
        final AtomicInteger countRunning = new AtomicInteger(0);
        while (!isInterrupted()) {
            if (requestCounter.getAndIncrement() > maxRequests) {
                LOG.log(INFO, "Already produced {0} requests, stopping the executor.", maxRequests);
                executor.shutdown();
                return;
            }
            if (countRunning.get() == maxParallel) {
                LOG.log(TRACE, "Waiting...");
                Thread.onSpinWait();
                continue;
            }
            LOG.log(DEBUG, () -> "Running: " + countRunning + ". Starting another...");
            countRunning.incrementAndGet();
            executor.submit(() -> {
                action.doAction();
                countRunning.decrementAndGet();
            });
        }
    }

    /**
     * Interrupts the thread is it is alive
     * Then waits until it terminates.
     * Then shuts down the executor pool.
     * Then waits until all threads finish.
     */
    @Override
    public void close() throws InterruptedException {
        // First interrupt the thread
        if (isAlive()) {
            synchronized(this) {
                interrupt();
            }
        }
        // ... then wait until it really terminates
        join();
        // ... then shutdown executor (but not its threads)
        executor.shutdown();
        // ... then wait until all threads finish.
        assertTrue(executor.awaitTermination(60, TimeUnit.SECONDS), "Timeout when terminating HTTP load generator!");
    }
}
