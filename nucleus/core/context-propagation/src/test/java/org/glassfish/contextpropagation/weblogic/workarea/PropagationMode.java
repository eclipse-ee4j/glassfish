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

public interface PropagationMode 
{
  /**
   * Propagate a WorkContext only for the scope of the current thread.
   */
  public static final int LOCAL = 1;

  /**
   * Propagate a WorkContext across Work instances.
   */
  public static final int WORK = 2;

  /**
   * Propagate a WorkContext across RMI invocations.
   */
  public static final int RMI = 4;

  /**
   * Propagate a WorkContext across global transactions.
   */
  public static final int TRANSACTION = 8;

  /**
   * Propagate a WorkContext to JMS queues.
   */
  public static final int JMS_QUEUE = 16;

  /**
   * Propagate a WorkContext to JMS topics.
   */
  public static final int JMS_TOPIC = 32;

  /**
   * Propagate a WorkContext across SOAP messages.
   */
  public static final int SOAP = 64;

  /**
   * Propagate a WorkContext from a MIME header or HTTP cookie.
   */
  public static final int MIME_HEADER = 128;

  /**
   * Propagate a WorkContext downstream from the caller only.
   */
  public static final int ONEWAY = 256;

  /**
   * Propagate a WorkContext across remote invocations and local
   * invocations in the same thread.
   */
  public static final int GLOBAL = RMI | JMS_QUEUE | SOAP | MIME_HEADER;

  /**
   * The default <code>PropagationMode</code> which is equivalent to
   * <code>GLOBAL</code>.
   */
  public static final int DEFAULT = GLOBAL;
}
