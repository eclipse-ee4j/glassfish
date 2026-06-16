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

import jakarta.faces.context.FacesContext;
import jakarta.faces.event.SystemEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * To get an instance of this class, use {@link #getInstance()}. This will check the
 * "<code>com.sun.jsft.TASK_MANAGER</code>" <code>web.xml</code> <code>context-param</code> to find the correct
 * implementation to use. If not specified, it will use the {@link DefaultTaskManager}. Alternatively, you can invoke
 * "setTaskManager(TaskManager)" directly to specify the desired implementation.
 * </p>
 */
public abstract class TaskManager {

    /**
     * <p>
     * Default constructor.
     * </p>
     */
    public TaskManager() {
        super();
    }

    /**
     * <p>
     * This method is responsible for executing the queued Tasks. It is possible this method may be called more than once
     * (not common), so care should be taken to ensure this is handled appropriately. This method is normally executed after
     * the page (excluding DefferedFragments, of course) have been rendered.
     * </p>
     */
    public abstract void start();

    /**
     * <p>
     * This method locates or creates the TaskManager instance associated with this request.
     * </p>
     */
    public static TaskManager getInstance() {
        // See if we already calculated the TaskManager for this request
        FacesContext ctx = FacesContext.getCurrentInstance();
        TaskManager taskManager = null;
        Map<String, Object> requestMap = null;
        if (ctx != null) {
            requestMap = ctx.getExternalContext().getRequestMap();
            taskManager = (TaskManager) requestMap.get(TASK_MANAGER);
        }
        if (taskManager == null) {
            Map initParams = ctx.getExternalContext().getInitParameterMap();
            String className = (String) initParams.get(IMPL_CLASS);
            if (className != null) {
                try {
                    taskManager = (TaskManager) Class.forName(className).newInstance();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                taskManager = new DefaultTaskManager();
            }
            if (requestMap != null) {
                requestMap.put(TASK_MANAGER, taskManager);
            }
        }
        return taskManager;
    }

    /**
     * <p>
     * This method is provided in case the developer would like to provide their own way to calculate and create the
     * <code>TaskManager</code> implementation to use.
     * </p>
     */
    public static void setTaskManager(TaskManager taskManager) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (ctx != null) {
            ctx.getExternalContext().getRequestMap().put(TASK_MANAGER, taskManager);
        } else {
            throw new RuntimeException("Currently only JSF is supported!  FacesContext not found.");
        }
    }

    /**
     * <p>
     * This method is responsible for queuing up a <code>task</code> to be performed. The given <code>newListeners</code>
     * will be fired according to the requested event <code>type</code>. If the <code>type</code> is not specified, it will
     * default to {@link Task#DEFAULT_EVENT_TYPE} indicating that the given <code>newListeners</code> should be fired at the
     * completion of the task.
     * </p>
     *
     * <p>
     * Note: If the <code>task</code> is already queued, it will NOT be performed twice. The <code>newListeners</code> will
     * be added to the already-queued <code>task</code>.
     * </p>
     *
     * @param task A unique string identifying a task to perform. This is implementation specific to the TaskManager
     * implementation.
     *
     * @param type Optional String identifying the event name within the task in which the given Listeners are associated.
     * If no type is given, the listeners will be fired at the end of the task ({@link Task#DEFAULT_EVENT_TYPE}).
     *
     * @param newListeners The SystemEventListener to be associated with this task and optional type if specified.
     */
    public void addTask(String taskName, String type, SystemEventListener... newListeners) {
// FIXME: Do I want to accept priority too??  Or perhaps that is handled in
// FIXME: the implementation-specific way tasks are registered?  Or is priority
// FIXME: only associated with DeferredFragments?
        Task task = tasks.get(taskName);
        if (task == null) {
            // New Task, create and add...
            task = new Task(taskName);
            task.setListeners(type, toArrayList(newListeners));
            tasks.put(taskName, task);
        } else {
            // Task already created, add the listeners for this type...
            List<SystemEventListener> taskListeners = task.getListeners(type);
            if (taskListeners == null) {
                task.setListeners(type, toArrayList(newListeners));
            } else {
                taskListeners.addAll(toArrayList(newListeners));
            }
        }
    }

    /**
     * <p>
     * This method returns the <code>List&lt;Task&gt;</code>.
     * </p>
     */
    public Collection<Task> getTasks() {
        return tasks.values();
    }

    /**
     * <p>
     * Convert an array of <code>T</code> to an <code>ArrayList&lt;T&gt;</code>.
     * </p>
     */
    private <T> ArrayList<T> toArrayList(T arr[]) {
        ArrayList<T> list = new ArrayList<>(arr.length);
        for (T item : arr) {
            list.add(item);
        }
        return list;
    }

    /**
     * <p>
     * This <code>Map</code> will hold all the Tasks.
     * </p>
     */
    private Map<String, Task> tasks = new HashMap<>(2);

    /**
     * <p>
     * The request scope key for holding the TASK_MANAGER instance to make it easily obtained.
     * </p>
     */
    private static final String TASK_MANAGER = "_jsftTM";

    /**
     * <p>
     * The web.xml <code>context-param</code> for declaring the implementation of this class to use.
     * </p>
     */
    public static final String IMPL_CLASS = "com.sun.jsft.TASK_MANAGER";
}
