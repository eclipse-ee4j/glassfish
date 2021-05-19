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


/**
 * Work contexts that implements this interface will be notified of
 * context propagation lifecycle events when a context is changed, added,
 * removed, or propagated.
 */
public interface ContextLifecycle extends ViewCapable {
  /**
   * Informs the receiver that a new context is taking its place in the
   * ContextMap
   * @param replacementContext The replacement context.
   */
  void contextChanged(Object replacementContext);

  /**
   * Informs this Context that it was added to the ContextMap. This
   * notification is particularly valuable when this Context was
   * automatically added to a ContextMap during deserialization.
   */
  void contextAdded();

  /**
   * Informs this Context that it was removed from the ContextMap
   */
  void contextRemoved();

  /**
   * Sent to a Context just before it is propagated.
   * @return The context to propagate. It could be this, another context, or
   * null if the receiver wants to prevent this context propagation.
   */
  ViewCapable contextToPropagate();
}
