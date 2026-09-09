/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package org.glassfish.orb.http.server;


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Tracks in-flight invocations so that a later {@code DELETE .../cancel/...}
 * can reach them.
 *
 * <h2>Why this exists when HTTP/2 can already reset a stream</h2>
 * Cancelling a {@code CompletableFuture} on the JDK client emits
 * {@code RST_STREAM}, and on HTTP/3 there is {@code STOP_SENDING}. Both are
 * free and immediate. But they say only "I no longer want the response" - the
 * server is entitled to carry on executing, and for a transactional business
 * method it usually should. Asking the container to stop working is an
 * application-level statement, and that is what the cancel operation makes.
 * Stream reset and this registry are complementary, not alternatives.
 */
public final class InvocationRegistry {

    private final ConcurrentMap<String, Registration> inFlight = new ConcurrentHashMap<>();

    /** A registered invocation; close it when the invocation completes. */
    public final class Registration implements AutoCloseable {

        private final String id;
        private final Thread thread;
        private volatile boolean cancelled;

        private Registration(String id, Thread thread) {
            this.id = id;
            this.thread = thread;
        }

        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void close() {
            if (id != null) {
                inFlight.remove(id, this);
            }
        }
    }

    /**
     * @param invocationId the client-minted id, or {@code null} if the client
     *                     did not send one - in which case the invocation is
     *                     simply not cancellable
     */
    public Registration register(String invocationId) {
        Registration registration = new Registration(invocationId, Thread.currentThread());
        if (invocationId != null) {
            inFlight.put(invocationId, registration);
        }
        return registration;
    }

    /**
     * @param interrupt whether to interrupt the executing thread as well as
     *                  mark the invocation cancelled
     * @return whether an invocation with that id was in flight
     */
    public boolean cancel(String invocationId, boolean interrupt) {
        Registration registration = inFlight.get(invocationId);
        if (registration == null) {
            // Either it already completed or it was never registered. A cancel
            // that loses the race is not an error.
            return false;
        }
        registration.cancelled = true;
        if (interrupt) {
            registration.thread.interrupt();
        }
        return true;
    }

    public int inFlightCount() {
        return inFlight.size();
    }
}
