/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

import jakarta.inject.Inject;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.EventListener.Event;
import org.glassfish.api.event.EventTypes;
import org.glassfish.api.event.Events;
import org.glassfish.api.event.RestrictTo;
import org.glassfish.deployment.common.DeploymentException;
import org.glassfish.kernel.KernelLoggerInfo;

/**
 * Simple implementation of the events dispatching facility.
 *
 * @author Jerome Dochez
 */
public class EventsImpl implements Events {

    private final static Logger LOG = KernelLoggerInfo.getLogger();

    private final AtomicLong sequenceGenerator = new AtomicLong(0L);

    /**
     * Use skip list based map to preserve listeners in registration order.
     */
    final Map<Listener, EventMatcher> listeners = new ConcurrentSkipListMap<>();

    @Inject
    private ExecutorService executor;

    @Override
    public void register(EventListener listener) {
        try {
            Method eventMethod = listener.getClass().getMethod("event", Event.class);
            RestrictTo[] restrictTo = eventMethod.getParameters()[0].getAnnotationsByType(RestrictTo.class);
            EventTypes<?>[] eventTypes = Arrays.stream(restrictTo)
                .map(restrict -> EventTypes.create(restrict.value())).toArray(EventTypes[]::new);
            listeners.putIfAbsent(new Listener(listener, sequenceGenerator.getAndIncrement()), new EventMatcher(eventTypes));
        } catch (Throwable t) {
            // We need to catch Throwable, otherwise we can server not to
            // shutdown when the following happens:
            // Assume a bundle which has registered an event listener
            // has been uninstalled without unregistering the listener.
            // listener.getClass() refers to a class of such an uninstalled
            // bundle. If framework has been refreshed, then the
            // classloader can't be used further to load any classes.
            // As a result, an exception like NoClassDefFoundError is thrown
            // from getMethod.
            LOG.log(Level.SEVERE, KernelLoggerInfo.exceptionRegisterEventListener, t);
        }
    }

    @Override
    public void send(final Event<?> event) {
        send(event, true);
    }

    @Override
    public void send(final Event<?> event, boolean asynchronously) {
        for (Map.Entry<Listener, EventMatcher> entry : listeners.entrySet()) {
            EventMatcher matcher = entry.getValue();
            // Check if the listener is interested with his event.
            if (matcher.matches(event)) {
                Listener listener = entry.getKey();
                if (asynchronously) {
                    executor.submit(() -> {
                        try {
                            listener.event(event);
                        } catch (Throwable t) {
                            LOG.log(Level.WARNING, KernelLoggerInfo.exceptionDispatchEvent, t);
                        }
                    });
                } else {
                    try {
                        listener.event(event);
                    } catch (DeploymentException e) {
                        // When synchronous listener throws DeploymentException
                        // we re-throw the exception to abort the deployment
                        throw e;
                    } catch (Throwable t) {
                        LOG.log(Level.WARNING, KernelLoggerInfo.exceptionDispatchEvent, t);
                    }
                }
            }
        }
    }

    @Override
    public boolean unregister(EventListener listener) {
        return listeners.remove(new Listener(listener)) != null;
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
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof Listener)) {
                return false;
            }
            return eventListener.equals(((Listener) obj).eventListener);
        }

        @Override
        public int hashCode() {
            return eventListener.hashCode();
        }

        @Override
        public int compareTo(Listener listener) {
            return Long.compare(sequenceNumber, listener.sequenceNumber);
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
