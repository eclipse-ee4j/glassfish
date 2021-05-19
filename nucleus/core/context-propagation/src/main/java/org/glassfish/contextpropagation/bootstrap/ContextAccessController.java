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

import org.glassfish.contextpropagation.InsufficientCredentialException;
import org.glassfish.contextpropagation.internal.AccessControlledMap;

public abstract class ContextAccessController {
  /**
   * Checks whether access of type <code>type</code> is allowed
   * on <code>key</code> based on the user associated to the
   * current thread.
   *
   * @param key the key to check access for
   * @param type the type of access required
   * @return true if access is allowed, false otherwise
   */
  public abstract boolean isAccessAllowed(String key,
      AccessControlledMap.ContextAccessLevel type);

  /**
   * Checks if access is allowed and throws an InsufficientCredentialException
   * if access is not allowed.
   * @throws InsufficientCredentialException if access could not be granted
   */
  public void checkAccessAllowed(String key,
      AccessControlledMap.ContextAccessLevel type)
          throws InsufficientCredentialException {
    if (!isAccessAllowed(key, type)) {
      throw new InsufficientCredentialException();
    }
  }

  /**
   * @param key The name of a context
   * @return true if everyone can read the context named by key
   */
  public abstract boolean isEveryoneAllowedToRead(String key);
}
