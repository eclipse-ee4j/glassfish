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

package org.glassfish.contextpropagation.weblogic.workarea;

import javax.naming.Context;
import javax.naming.NamingException;

import org.glassfish.contextpropagation.weblogic.workarea.spi.WorkContextAccessController;
import org.glassfish.contextpropagation.weblogic.workarea.spi.WorkContextMapInterceptor;



/**
 * <code>WorkContextHelper</code> allows internal users to obtain and
 * modify {@link WorkContext}s. The class APIs allow for
 * replacement of the implementation - although there are currently no
 * use-cases for this. Typical usages follow. To obtain the current
 * {@link WorkContextMap} for update:
 * <p> <pre>
 * WorkContextMap interceptor
 *   = WorkContextHelper.getWorkContextHelper().getWorkContextMap();
 *</pre>
 *
 * To obtain a {@link org.glassfish.contextpropagation.weblogic.workarea.spi.WorkContextMapInterceptor}:
 * <p> <pre>
 * WorkContextMapInterceptor interceptor
 *   = WorkContextHelper.getWorkContextHelper().getInterceptor();
 *</pre>
 *
 * @exclude
 * @see org.glassfish.contextpropagation.weblogic.workarea.WorkContextMap
 * @see org.glassfish.contextpropagation.weblogic.workarea.spi.WorkContextMapInterceptor
 *
 */
public class WorkContextHelper
{
  private static final String WORK_CONTEXT_BINDING = "WorkContextMap";
  private static final WorkContextMapImpl map = new WorkContextMapImpl();

  private static WorkContextHelper singleton = new WorkContextHelper();

  // Prevent outsiders creating one.
  protected WorkContextHelper() { }

  /**
   * Get the WorkContextHelper singleton.
   *
   * @return A suitable WorkContextHelper implementation for client or
   * server.
   */
  public static WorkContextHelper getWorkContextHelper() {
    return singleton;
  }

  /**
   * Set the WorkContextHelper singleton. This should be set at startup
   * and not synchronized.
   *
   * @param wam - a suitable WorkContextHelper implementation for client
   * or server.
   */
  public static void setWorkContextHelper(WorkContextHelper wam) {
    throw new IllegalArgumentException
      ("WorkContextHelper does not currently support replacement");
  }

  public WorkContextMap getWorkContextMap() {
    return map;
  }

  //This is F&F API introduced for Oracle DMS for faster WorkContext reads
  //PriviledgedWorkContextMap does a read as KernelId for the given two
  //DMS specific WorkContext keys
  public WorkContextMap getPriviledgedWorkContextMap() {
    return WorkContextAccessController.getPriviledgedWorkContextMap(map);
  }

  /**
   * Get the singleton instance of the current
   * {@link org.glassfish.contextpropagation.weblogic.workarea.spi.WorkContextMapInterceptor}..
   *
   * @return A suitable WorkContextMapInterceptor implementation for
   * client or server.
   */
  public WorkContextMapInterceptor getInterceptor() {
    return map;
  }

  /**
   * Get the singleton thread-local instance of the current
   * {@link org.glassfish.contextpropagation.weblogic.workarea.spi.WorkContextMapInterceptor}, or null if there is no
   * {@link WorkContextMap} for the current thread.
   *
   * @return A suitable WorkContextMapInterceptor implementation for
   * client or server.
   */
  public WorkContextMapInterceptor getLocalInterceptor() {
    return map.getInterceptor();
  }

  /**
   * Create an instance of {@link
   * org.glassfish.contextpropagation.weblogic.workarea.spi.WorkContextMapInterceptor} for the purposes
   * of serialization. Thread infection can be achieved via {@link
   * #setLocalInterceptor}.
   *
   * @return A suitable WorkContextMapInterceptor implementation for
   * client or server.
   */
  public WorkContextMapInterceptor createInterceptor() {
    return new WorkContextLocalMap();
  }

  /**
   * Take an {@link org.glassfish.contextpropagation.weblogic.workarea.spi.WorkContextMapInterceptor}
   * object and make it the current map. This bypasses serialization
   * schemes for the {@link WorkContextMap}. This allows callers to
   * separate serialization from thread infection and vice versa.
   */
  public void setLocalInterceptor(WorkContextMapInterceptor interceptor) {
    map.setInterceptor(interceptor);
  }

  public static void bind(Context ctx) throws NamingException {
    ctx.bind(WORK_CONTEXT_BINDING, getWorkContextHelper().getWorkContextMap());
  }

  public static void unbind(Context ctx) throws NamingException {
    ctx.unbind(WORK_CONTEXT_BINDING);
  }
}
