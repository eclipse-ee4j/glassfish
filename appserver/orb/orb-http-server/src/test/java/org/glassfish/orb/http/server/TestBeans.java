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





import java.io.Serializable;
import java.util.List;
import java.util.Map;

/** The remote views and bean implementations used by the round-trip test. */
final class TestBeans {

    private TestBeans() {
    }

    /**
     * Run by {@link GreeterBean#notifyArrival} while the invocation is in
     * progress, so a test can observe server side state at the moment the bean
     * is executing rather than only before and after.
     */
    static volatile Runnable duringInvocation;

    /** A stateless remote business interface. */
    interface Greeter {

        String greet(String name, int times);

        void notifyArrival(String name);

        Map<String, List<Integer>> cyclicFriendlyPayload(int size);

        String echoLarge(String payload);

        /** An EJB asynchronous method: declared to return a Future. */
        java.util.concurrent.Future<String> greetLater(String name);

        void refuse();

        /** Throws something unchecked and unannotated: a system failure. */
        void explode();
    }

    /** A stateful one. */
    interface Counter {

        int increment();

        int value();
    }

    /**
     * An application exception carrying business state. Its faithful arrival is
     * the property a JSON-based transport cannot offer.
     *
     * <p>The annotation is load-bearing: without it this is unchecked and
     * undeclared, which the specification makes a system exception - the
     * container would wrap it in EJBException and the caller would never see
     * this type. An IIOP baseline running against a real server is what made
     * that concrete.
     */
    @jakarta.ejb.ApplicationException(rollback = true)
    static final class GreetingRefused extends RuntimeException {

        private static final long serialVersionUID = 1L;

        private final String reasonCode;

        GreetingRefused(String message, String reasonCode, Throwable cause) {
            super(message, cause);
            this.reasonCode = reasonCode;
        }

        String reasonCode() {
            return reasonCode;
        }
    }

    static final class GreeterBean implements Greeter {

        @Override
        public String greet(String name, int times) {
            return (name + ' ').repeat(times).trim();
        }

        @Override
        public void notifyArrival(String name) {
            // void return, exercised on purpose
            Runnable probe = duringInvocation;
            if (probe != null) {
                probe.run();
            }
        }

        @Override
        public Map<String, List<Integer>> cyclicFriendlyPayload(int size) {
            return Map.of("numbers", java.util.stream.IntStream.range(0, size).boxed().toList());
        }

        @Override
        public String echoLarge(String payload) {
            return payload;
        }

        @Override
        public java.util.concurrent.Future<String> greetLater(String name) {
            // The container is what makes this asynchronous; the bean returns
            // its result the ordinary way.
            return java.util.concurrent.CompletableFuture.completedFuture("later " + name);
        }

        @Override
        public void explode() {
            throw new IllegalStateException("an unchecked exception the bean never declared");
        }

        @Override
        public void refuse() {
            throw new GreetingRefused("not today", "E_CLOSED",
                    new IllegalStateException("underlying cause"));
        }
    }

    static final class CounterBean implements Counter, Serializable {

        private static final long serialVersionUID = 1L;

        private int count;

        @Override
        public int increment() {
            return ++count;
        }

        @Override
        public int value() {
            return count;
        }
    }
}
