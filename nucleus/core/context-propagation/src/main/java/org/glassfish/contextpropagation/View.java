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

package org.glassfish.contextpropagation;

import java.util.EnumSet;

/**
 * A View provides access to a subset of contexts that share a common prefix
 * that is the key for a ViewCapable instance.
 * View is used by Implementors of ViewCapable.
 */
public interface View {
  /**
   *
   * @param name The name of the context sought.
   * @return The context associated to the specified name.
   * @throws InsufficientCredentialException If the user has insufficient
   * privileges to access that context.
   */
  <T> T get(String name);

  /**
   * Stores the specified context under the specified name into the in-scope ContextMap.
   * @param name The name to associate to the specified context
   * @param context a String context.
   * @param propagationModes A set of propagation modes that control over
   * which protocol this context will be propagated.
   * @return The context being replaced.
   * @throws InsufficientCredentialException If the user has insufficient
   * privileges to access that context.
   */
  <T> T put(String name, String context, EnumSet<PropagationMode> propagationModes);

  /**
   * Stores the specified context under the specified name into the in-scope ContextMap.
   * @param name The name to associate to the specified context
   * @param context a Number context.
   * @param propagationModes A set of propagation modes that control over
   * which protocol this context will be propagated.
   * @return The context being replaced.
   * @throws InsufficientCredentialException If the user has insufficient
   * privileges to access that context.
   */
  <T, U extends Number> T put(String name, U context, EnumSet<PropagationMode> propagationModes);

  /**
   * Stores the specified context under the specified name into the in-scope ContextMap.
   * @param name The name to associate to the specified context
   * @param context an boolean String context.
   * @param propagationModes A set of propagation modes that control over
   * which protocol this context will be propagated.
   * @return The context being replaced.
   * @throws InsufficientCredentialException If the user has insufficient
   * privileges to access that context.
   */
  <T> T put(String name, Boolean context, EnumSet<PropagationMode> propagationModes);

  /**
   * Stores the specified context under the specified name into the in-scope ContextMap.
   * @param name The name to associate to the specified context
   * @param context an char String context.
   * @param propagationModes A set of propagation modes that control over
   * which protocol this context will be propagated.
   * @return The context being replaced.
   * @throws InsufficientCredentialException If the user has insufficient
   * privileges to access that context.
   */
  <T> T put(String name, Character context, EnumSet<PropagationMode> propagationModes);

   /**
    * Removes the specified context under the specified name from the in-scope ContextMap.
    * @param name The name to associate to the specified context
    * @return The context being replaced.
    * @throws InsufficientCredentialException If the user has insufficient
    * privileges to access that context.
    */
   <T> T remove(String name);
}
