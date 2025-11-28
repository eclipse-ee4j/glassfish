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

package com.sun.enterprise.admin.servermgmt.cli;

import com.sun.enterprise.universal.process.ProcessUtils;
import com.sun.enterprise.util.HostAndPort;

import java.lang.System.Logger;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import static java.lang.System.Logger.Level.TRACE;


/**
 * Use case:
 * <ol>
 * <li>We have a running server listening on some endpoint
 * <li>We will watch it until it closes the endpoint.
 * <li>In parallel to the watching we ask the server to stop.
 * </ol>
 * Why - if we would start watching after the command, endpoint could be already in some state
 * when it would refuse new connections, but it would also refuse to rebind on the server side
 * of the new process. So we need to be sure the endpoint stopped listening before we start
 * binding again.
 */
public class PortWatcher {
    private static final Logger LOG = System.getLogger(PortWatcher.class.getName());

    private final CompletableFuture<Boolean> job;

    private PortWatcher(Supplier<Boolean> supplier) {
        this.job = CompletableFuture.supplyAsync(supplier);
        // We should be always sure we do listen to the old process and not to the new one.
        // If it starts to be flaky, replace with sleep.
        Thread.onSpinWait();
    }

    /**
     * Blocks until we have the positive answer or endpoint is still listening after timeout.
     *
     * @param timeout can be null, then we may wait forever.
     * @return true if the endpoint disconnected before timeout, false if it timed out.
     */
    public boolean get(Duration timeout) {
        LOG.log(TRACE, "get(timeout={0})", timeout);
        try {
            if (timeout == null) {
                return job.get();
            }
            return job.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            job.cancel(true);
            return false;
        }
    }

    /**
     * Watch the endpoint in a separate thread.
     *
     * @param endpoint
     * @param printDots
     * @return the {@link PortWatcher}
     */
    public static PortWatcher watch(HostAndPort endpoint, boolean printDots) {
        Objects.requireNonNull(endpoint, "endpoint");
        return new PortWatcher(() -> ProcessUtils.waitWhileListening(endpoint, null, printDots));
    }
}
