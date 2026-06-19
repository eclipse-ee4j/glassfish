/*
 * Copyright (c) 2011, 2020 Oracle and/or its affiliates. All rights reserved.
 * Portions Copyright (c) 2011 Ken Paulsen
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

package com.sun.jsft.tasks;

import jakarta.faces.event.SystemEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * This class holds the representation of a single task.
 * </p>
 */
public class Task {

    /**
     * <p>
     * Default constructor.
     * </p>
     */
    public Task() {
        super();
    }

    /**
     * <p>
     * Constructor w/ <code>name</code>.
     * </p>
     */
    public Task(String name) {
        this();
        setName(name);
    }

    /**
     * <p>
     * The name used to identify the task. In some instances, the name may define the task (i.e. EL).
     * </p>
     */
    public String getName() {
        return name;
    }

    /**
     *
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     */
    public List<SystemEventListener> getListeners(String type) {
        if (type == null) {
            type = DEFAULT_EVENT_TYPE;
        }
        return listenersByType.get(type);
    }

    /**
     *
     */
    public void setListeners(String type, List<SystemEventListener> listeners) {
        if (type == null) {
            type = DEFAULT_EVENT_TYPE;
        }
        listenersByType.put(type, listeners);
    }

    // The identifier for this Task
    private String name = "";

    // Map of List to store the events by type
    private Map<String, List<SystemEventListener>> listenersByType = new HashMap<>(2);

    /**
     * <p>
     * The default event type.
     * </p>
     */
    public static final String DEFAULT_EVENT_TYPE = TaskEvent.TASK_COMPLETE;
}
