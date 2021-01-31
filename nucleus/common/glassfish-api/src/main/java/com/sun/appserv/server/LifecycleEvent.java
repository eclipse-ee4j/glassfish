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

package com.sun.appserv.server;

/**
 * This class defines the types of events that get fired by the application server. It also contains a
 * LifecycleEventContext that can be used by the lifecycle modules.
 */
public class LifecycleEvent extends java.util.EventObject {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private int eventType;
    private Object eventData;
    private transient LifecycleEventContext ctx = null;

    // Lifecycle event types

    /**
     * Server is initializing subsystems and setting up the runtime environment.
     */
    public final static int INIT_EVENT = 0;

    /**
     * Server is starting up applications
     */
    public final static int STARTUP_EVENT = 1;

    /**
     * Server is ready to service requests
     */
    public final static int READY_EVENT = 2;

    /**
     * Server is shutting down applications
     */
    public final static int SHUTDOWN_EVENT = 3;

    /**
     * Server is terminating the subsystems and the runtime environment.
     */
    public final static int TERMINATION_EVENT = 4;

    /**
     * Construct new lifecycle event
     *
     * @param source The object on which the event initially occurred
     * @param eventType type of the event
     * @param ctx the underlying context for the lifecycle event
     */
    public LifecycleEvent(Object source, int eventType, Object eventData, LifecycleEventContext ctx) {
        super(source);

        this.eventType = eventType;
        this.eventData = eventData;
        this.ctx = ctx;
    }

    /**
     * Get the type of event associated with this
     */
    public int getEventType() {
        return eventType;
    }

    /**
     * Get the data associated with the event.
     */
    public Object getData() {
        return eventData;
    }

    /**
     * Get the ServerContext generating this lifecycle event
     */
    public LifecycleEventContext getLifecycleEventContext() {
        return ctx;
    }
}
