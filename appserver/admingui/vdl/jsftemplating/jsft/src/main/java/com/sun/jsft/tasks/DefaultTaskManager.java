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

import jakarta.faces.event.SystemEvent;
import jakarta.faces.event.SystemEventListener;

import java.util.List;

/**
 * <p>
 * This is the default {@link TaskManager} implementation.
 * </p>
 */
public class DefaultTaskManager extends TaskManager {

    /**
     * <p>
     * Default constructor.
     * </p>
     */
    protected DefaultTaskManager() {
        super();
    }

    /**
     * <p>
     * This method is responsible for executing the queued Tasks. It is possible this method may be called more than once
     * (not common), so care should be taken to ensure this is handled appropriately. This method is normally executed after
     * the page (excluding DefferedFragments, of course) have been rendered.
     * </p>
     */
    @Override
    public void start() {
        System.out.println("Starting to execute Tasks: " + getTasks());
        // Loop through the tasks and execute them...
        for (Task task : getTasks()) {
// FIXME: This implementation is a no-op, it just loops through the tasks and fires the TASK_COMPLETE event.
// FIXME: A real implementation would aggregate & dispatch the tasks and register listeners with the "backend dispatcher" which would fire the TASK_COMPLETE event.
// FIXME: This method should not block.
            SystemEvent event = new TaskEvent(task);
            List<SystemEventListener> listeners = task.getListeners(TaskEvent.TASK_COMPLETE);
            if (listeners != null) {
                for (SystemEventListener listener : listeners) {
                    listener.processEvent(event);
                }
            }
        }
    }
}
