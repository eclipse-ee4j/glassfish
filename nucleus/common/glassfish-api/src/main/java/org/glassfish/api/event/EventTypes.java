/*
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

/**
 * Extensible list of event types. EventTypes are created through the create method and not directly.
 *
 * Events can be compared using == or equals although == is recommended.
 *
 * @author dochez
 */
public final class EventTypes<T> {

    private final static Map<String, EventTypes> EVENTS = new HashMap<>();

    // stock events.
    public static final String SERVER_STARTUP_NAME = "server_startup";
    public static final String SERVER_READY_NAME = "server_ready";
    public static final String PREPARE_SHUTDOWN_NAME = "prepare_shutdown";
    public static final String SERVER_SHUTDOWN_NAME = "server_shutdown";

    public static final EventTypes SERVER_STARTUP = create(SERVER_STARTUP_NAME);
    public static final EventTypes SERVER_READY = create(SERVER_READY_NAME);
    public static final EventTypes SERVER_SHUTDOWN = create(SERVER_SHUTDOWN_NAME);
    public static final EventTypes PREPARE_SHUTDOWN = create(PREPARE_SHUTDOWN_NAME);

    public static EventTypes create(String name) {
        return create(name, null);
    }

    public static <T> EventTypes<T> create(String name, Class<T> hookType) {
        synchronized (EVENTS) {
            if (!EVENTS.containsKey(name)) {
                EVENTS.put(name, new EventTypes(name, hookType));
            }
        }
        return EVENTS.get(name);
    }

    private final String name;
    private final Class<T> hookType;

    private EventTypes(String name, Class<T> hookType) {
        this.name = name;
        this.hookType = hookType;
    }

    public String type() {
        return name;
    }

    public Class<T> getHookType() {
        return hookType;
    }

    public T getHook(EventListener.Event<T> e) {
        if (e.is(this)) {
            return e.hook();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Considers only {@link #name} for equality.
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (null == o) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return false;
        }

        return name.equals(((EventTypes) o).name);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Returns {@link #name} as the hash code.
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }

}
