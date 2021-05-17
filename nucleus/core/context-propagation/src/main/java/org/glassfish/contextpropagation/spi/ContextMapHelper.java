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

package org.glassfish.contextpropagation.spi;

import org.glassfish.contextpropagation.ContextMap;
import org.glassfish.contextpropagation.ContextViewFactory;
import org.glassfish.contextpropagation.internal.Utils;

/**
 * Provides internal access to context-propagation artifacts. Application
 * developers should retrieve the ContextMap from the JNDI instead.
 */
public class ContextMapHelper {
  /**
   *
   * @return The in-scope ContextMap.
   */
  public static ContextMap getScopeAwareContextMap() {
    return Utils.getScopeAwareContextMap();
  }

  /**
   *
   * @return The in-scope instance of ContextMapPropagator so that
   * communication protocols can ask the ContextMapPropagator to handle
   * the context propagation bytes on the wire.
   */
  public static ContextMapPropagator getScopeAwarePropagator() {
    return Utils.getScopeAwarePropagator();
  }

  /**
   * ViewCapable objects are created by the context propagation framework
   * when needed using the ContextViewFactory registered against the
   * specified context name
   * @param prefixName This is the name of the context that should be instantiated
   * with the corresponding factory.
   * @param factory A ContextViewFactory.
   */
  public static void registerContextFactoryForPrefixNamed(String prefixName, ContextViewFactory factory) {
    Utils.registerContextFactoryForPrefixNamed(prefixName, factory);
  }
}
