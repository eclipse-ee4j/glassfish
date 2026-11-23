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

import org.glassfish.orb.http.protocol.ProtocolException;

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
        // Both guarded by this: whether the invocation is over, and whether a
        // cancel interrupted its thread.
        private boolean closed;
        private boolean interruptedByCancel;

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
            synchronized (this) {
                closed = true;
                if (interruptedByCancel && thread == Thread.currentThread()) {
                    // The interrupt was meant for this invocation. The thread
                    // belongs to a pool and runs someone else's request next;
                    // handing it over interrupted would fail that one instead.
                    Thread.interrupted();
                }
            }
        }

        /**
         * The work is over: a cancel arriving from now on marks it cancelled
         * but interrupts nothing, and an interrupt already delivered is
         * cleared before the response is written.
         */
        public void finish() {
            synchronized (this) {
                closed = true;
                if (interruptedByCancel && thread == Thread.currentThread()) {
                    Thread.interrupted();
                }
            }
        }

        private synchronized void cancel(boolean interrupt) {
            cancelled = true;
            if (interrupt && !closed) {
                interruptedByCancel = true;
                thread.interrupt();
            }
        }
    }

    /**
     * @param invocationId the client-minted id, or {@code null} if the client
     *                     did not send one - in which case the invocation is
     *                     simply not cancellable
     */
    public Registration register(String invocationId) throws ProtocolException {
        Registration registration = new Registration(invocationId, Thread.currentThread());
        if (invocationId != null && inFlight.putIfAbsent(invocationId, registration) != null) {
            // Overwriting would let one caller take over another's
            // cancellation.
            throw new ProtocolException("an invocation with this id is already in flight");
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
        registration.cancel(interrupt);
        return true;
    }

    public int inFlightCount() {
        return inFlight.size();
    }
}
