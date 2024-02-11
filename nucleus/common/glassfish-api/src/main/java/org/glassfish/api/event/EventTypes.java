/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package org.glassfish.api.event;

import java.util.HashMap;
import java.util.Map;

import org.glassfish.api.event.EventListener.Event;

/**
 * Extensible list of event types.
 *
 * <p>{@code EventTypes} are created through one of the {@code create} methods and not directly.
 *
 * <p>Events can be compared using {@code ==} or {@link #equals} although {@code ==} is recommended.
 *
 * @author dochez
 */
public final class EventTypes<T> {

    private final static Map<String, EventTypes<?>> EVENTS = new HashMap<>();

    // Stock events.
    public static final String SERVER_STARTUP_NAME = "server_startup";
    public static final String SERVER_READY_NAME = "server_ready";
    public static final String PREPARE_SHUTDOWN_NAME = "prepare_shutdown";
    public static final String SERVER_SHUTDOWN_NAME = "server_shutdown";

    public static final EventTypes<?> SERVER_STARTUP = create(SERVER_STARTUP_NAME);
    public static final EventTypes<?> SERVER_READY = create(SERVER_READY_NAME);
    public static final EventTypes<?> SERVER_SHUTDOWN = create(SERVER_SHUTDOWN_NAME);
    public static final EventTypes<?> PREPARE_SHUTDOWN = create(PREPARE_SHUTDOWN_NAME);

    private final String name;
    private final Class<T> hookType;

    private EventTypes(String name, Class<T> hookType) {
        this.name = name;
        this.hookType = hookType;
    }

    public static EventTypes<?> create(String name) {
        return create(name, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> EventTypes<T> create(String name, Class<T> hookType) {
        synchronized (EVENTS) {
            if (!EVENTS.containsKey(name)) {
                EVENTS.put(name, new EventTypes<>(name, hookType));
            }
        }
        return (EventTypes<T>) EVENTS.get(name);
    }

    public String type() {
        return name;
    }

    public Class<T> getHookType() {
        return hookType;
    }

    public T getHook(Event<T> event) {
        if (event.is(this)) {
            return event.hook();
        }
        return null;
    }

    /**
     * Considers only {@link #name} for equality.
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof EventTypes)) {
            return false;
        }

        return name.equals(((EventTypes<?>) obj).name);
    }

    /**
     * Returns {@link #name} as the hash code.
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
