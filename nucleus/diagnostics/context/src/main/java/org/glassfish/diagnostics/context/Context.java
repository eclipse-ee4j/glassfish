/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.diagnostics.context;

import org.glassfish.contextpropagation.Location;

/**
 * The diagnostics Context is the object through which
 * diagnostics data relevant to the current task are
 * read and written.
 *
 * A task is a unit of work being executed by the java
 * process. Examples include
 * <ul>
 *      <li>responding to an external stimulus such
 * as a simple http request or web-service request</li>
 *     <li>executing a scheduled job</li>
 * </ul>
 * A parent task can create sub-tasks the completion of which
 * may or may not affect the execution of the parent task.
 * The diagnostics Context of the parent task will propagate
 * to the child sub-tasks.
 *
 * Diagnostics data include:
 * <ul>
 *     <li>Location: {@link org.glassfish.contextpropagation.Location}
 *     provides correlation between a task and its sub-task(s)</li>
 *     <li>Name-value pairs: Arbitrary name-value pairs that may
 *     be reported in diagnostics features such as logging,
 *     flight recordings, request sampling and tracing and so on.
 *     <ul>
 *         <li>Name: The name should use the standard java naming
 *         convention including package name. The name should
 *         be sufficiently clear that consumers of the data
 *         (e.g. the readers of log files, i.e. developers!)
 *         have some good starting point when interpreting the
 *         diagnostics data.</li>
 *         <li>Value: The value should be as concise as possible.</li>
 *     </ul>
 *     Only those name-value pairs marked for propagation will
 *     propagate to the diagnostics Contexts of sub-tasks.
 *     It is generally the case that data associated with a
 *     particular name will either always propagate or always
 *     not propagate - i.e. it is either usefully shared with
 *     child Contexts or only makes sense if kept private
 *     to one Context.
 *     </li>
 * </ul>
 *
 * The diagnostics Context of the currently executing task can
 * be obtained from the {@link ContextManager}.
 *
 * @see org.glassfish.contextpropagation.Location
 */
public interface Context
{
 /**
  * Get the Location of this Context.
  */
  public Location getLocation();

 /**
  * Put a named value in this Context.
  *
  * @param name The name of the item of data.
  * @param value The value of item of data.
  * @param propagates If true then the data item will be propagated.
  * @return The previous value associated with this name (if there is one).
  */
  public <T> T put(String name, String value, boolean propagates);

  /**
   * Put a named value in this Context.
   *
   * @param name The name of the item of data.
   * @param value The value of item of data.
   * @param propagates If true then the data item will be propagated.
   * @return The previous value associated with this name (if there is one).
   */
  public <T> T put(String name, Number value, boolean propagates);

  /**
   * Remove the named value from this Context.
   *
   * @param  name The name of the item of data.
   * @return The value being removed if there is one, otherwise null.
   */
  public <T> T remove(String name);

  /**
  * Get a named value from this Context.
  *
  * @param  name The name of the item of data.
  * @return The value associated with this name if there is one,
  *         otherwise null.
  */
  public <T> T get(String name);
}
