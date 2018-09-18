/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.contextpropagation.bootstrap;

import org.glassfish.contextpropagation.internal.AccessControlledMap;

/**
 * Used to integrate with the thread management system to safely save to
 * and retrieve the ContextMap associated to the current thread.
 */
public interface ThreadLocalAccessor {
  /**
   * Saves the ContextMap on the current thread.
   * @param contextMap
   */
  public void set(AccessControlledMap contextMap);

  /**
   * @return the ContextMap saved on the current thread.
   */
  public AccessControlledMap get();
}
