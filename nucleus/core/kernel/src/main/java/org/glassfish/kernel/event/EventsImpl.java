/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.kernel.event;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.EventListener.Event;
import org.glassfish.api.event.EventTypes;
import org.glassfish.api.event.Events;
import org.glassfish.api.event.RestrictTo;
import org.glassfish.deployment.common.DeploymentException;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.kernel.KernelLoggerInfo;

import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

/**
 * Simple implementation of the events dispatching facility.
 *
 * @author Jerome Dochez
 */
public class EventsImpl implements Events, PostConstruct {

    private final static Logger LOG = KernelLoggerInfo.getLogger();

    private final AtomicLong sequenceGenerator = new AtomicLong(0L);

    /**
     * Use skip list based map to preserve listeners in registration order.
     */
    final Map<Listener, EventMatcher> listenersBySequence = new ConcurrentSkipListMap<>();
    final Map<EventListener, Listener> listenersByIdentity = new ConcurrentHashMap<>();

    private ExecutorService executor;


    @Override
    public void postConstruct() {
        ThreadFactory threadFactory = r -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            t.setName("events-" + t.getId());
            return t;
        };
        executor = Executors.newCachedThreadPool(threadFactory);
    }

    @Override
    public void register(EventListener listener) {
        try {
            RestrictTo[] restrictTo =
                    listener.getClass()
                            .getMethod("event", Event.class)
                            .getParameters()[0]
                            .getAnnotationsByType(RestrictTo.class);

            listenersByIdentity.computeIfAbsent(listener, e -> {

                Listener listenerWrapperWithSeq = new Listener(listener, sequenceGenerator.getAndIncrement());

                EventMatcher eventMatcher = new EventMatcher(
                    Arrays.stream(restrictTo)
                          .map(restrict -> EventTypes.create(restrict.value()))
                          .toArray(EventTypes[]::new));

                listenersBySequence.put(
                    listenerWrapperWithSeq,
                    eventMatcher);

                return listenerWrapperWithSeq;
            });


        } catch (Throwable t) {
            // We need to catch Throwable, otherwise we can not
            // shutdown the server when the following happens:
            //
            // Assume a bundle which has registered an event listener
            // has been uninstalled without unregistering the listener.
            //
            // listener.getClass() refers to a class of such an uninstalled
            // bundle. If the framework has been refreshed, then the
            // classloader can't be used further to load any classes.
            //
            // As a result, an exception like NoClassDefFoundError is thrown
            // from getMethod.
            LOG.log(SEVERE, KernelLoggerInfo.exceptionRegisterEventListener, t);
        }
    }

    @Override
    public void send(final Event<?> event) {
        send(event, true);
    }

    @Override
    public void send(final Event<?> event, boolean asynchronously) {
        for (var entry : listenersBySequence.entrySet()) {

            // Check if the listener is interested with his event.
            if (entry.getValue().matches(event)) {

                Listener listener = entry.getKey();

                if (asynchronously) {
                    // Process Async
                    executor.submit(() -> {
                        try {
                            listener.event(event);
                        } catch (Throwable t) {
                            LOG.log(WARNING, KernelLoggerInfo.exceptionDispatchEvent, t);
                        }
                    });
                } else {
                    // Process sync
                    try {
                        listener.event(event);
                    } catch (DeploymentException e) {
                        // When synchronous listener throws DeploymentException
                        // we re-throw the exception to abort the deployment
                        throw e;
                    } catch (Throwable t) {
                        LOG.log(WARNING, KernelLoggerInfo.exceptionDispatchEvent, t);
                    }
                }
            }
        }
    }

    @Override
    public boolean unregister(EventListener listener) {
        Listener listenerWrapperWithSeq = listenersByIdentity.remove(listener);
        if (listenerWrapperWithSeq == null) {
            return false;
        }

        return listenersBySequence.remove(listenerWrapperWithSeq) != null;
    }

    /**
     * Comparable listener wrapper.
     *
     * <p>Need to dispatch events in the listener registration order.
     */
    static class Listener implements Comparable<Listener> {

        private final EventListener eventListener;
        private final long sequenceNumber;

        Listener(EventListener eventListener) {
            this(eventListener, -1L);
        }

        Listener(EventListener eventListener, long sequenceNumber) {
            this.eventListener = eventListener;
            this.sequenceNumber = sequenceNumber;
        }

        void event(Event<?> event) {
            eventListener.event(event);
        }

        EventListener unwrap() {
            return eventListener;
        }

        @Override
        public boolean equals(Object obj) {
            return
                obj instanceof Listener listener &&
                this.eventListener == listener.eventListener;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(eventListener);
        }

        @Override
        public int compareTo(Listener listener) {
            // Treat the same underlying listener as the SAME KEY
            if (this.eventListener == listener.eventListener) {
                // Identity
                return 0;
            }

            // otherwise keep registration order via sequence
            return Long.compare(this.sequenceNumber, listener.sequenceNumber);
        }
    }

    /**
     * A class that perform match operations on events.
     */
    static class EventMatcher {

        private final EventTypes<?>[] eventTypes;

        EventMatcher(EventTypes<?>[] eventTypes) {
            this.eventTypes = eventTypes;
        }

        boolean matches(Event<?> event) {
            if (eventTypes.length == 0) {
                return true;
            }
            return Arrays.stream(eventTypes).anyMatch(event::is);
        }
    }
}
