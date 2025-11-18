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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.asadmin.Asadmin;

import static java.lang.System.Logger.Level.DEBUG;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AsadminLoadGenerator extends Thread implements AutoCloseable {
    private static final Logger LOG = System.getLogger(AsadminLoadGenerator.class.getName());

    private final Asadmin asadmin = GlassFishTestEnvironment.getAsadmin(true);
    private final AtomicInteger threadCounter = new AtomicInteger();
    private final int maxParallel = 5;
    private final ExecutorService executor;

    AsadminLoadGenerator() {
        setName("AsadminLoadGenerator");
        executor = Executors.newFixedThreadPool(maxParallel,
            t -> new Thread(t, "AsadminClient-" + threadCounter.incrementAndGet()));
    }

    @Override
    public void run() {
        final AtomicInteger countRunning = new AtomicInteger();
        while (!isInterrupted()) {
            if (countRunning.get() > maxParallel) {
                Thread.onSpinWait();
                continue;
            }
            LOG.log(DEBUG, () -> "Running: " + countRunning + ". Starting another...");
            countRunning.incrementAndGet();
            executor.submit(() -> {
                asadmin.exec("list", "applications");
                asadmin.exec("get", "server.monitoring-service.*");
                countRunning.decrementAndGet();
            });
        }
    }

    @Override
    public void close() throws InterruptedException {
        if (isAlive()) {
            synchronized(this) {
                this.interrupt();
            }
        }
        join();
        executor.shutdown();
        assertTrue(executor.awaitTermination(60, TimeUnit.SECONDS),
            "Timeout when terminating asadmin load generator!");
    }
}
