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
 * The propagation mode determines if a work context is propagated. The creator
 * of a work context declares a set of propagation modes that is allowed by
 * the context. When protocols send or receive messages, they will propagate
 * any work context that includes the protocol's specific propagation mode.
 *  - LOCAL Context lives only in the current thread
 *  - THREAD Context will be propagated from thread to thread
 *  - RMI propagation along RMI messages
 *  - JMS_QUEUE propagation to JMS queues
 *  - JMS_TOPIC propagation to JMS topics
 *  - SOAP propagation along a SOAP message
 *  - MIME_HEADER propagation from a mime header or http cookie
 *  - ONEWAY propagation in requests only, not in responses.
 */
 /*  For future implementation
 *  - SECRET indicates that the context should not be exposed via the ContextMap APIs, it could be used to hide the individual constituents of a ViewCapable
 *  - IIOP propagation with IIOP messages
 */
public enum PropagationMode {
  LOCAL, THREAD, RMI, TRANSACTION, JMS_QUEUE, JMS_TOPIC, SOAP, MIME_HEADER,
  ONEWAY; /*SECRET, IIOP, CUSTOM; think about extension */
  private static PropagationMode[] byOrdinal = createByOrdinal();

  private static PropagationMode[] createByOrdinal() {
    PropagationMode[] values = values();
    PropagationMode[] byOrdinal = new PropagationMode[values.length];
    for (PropagationMode value : values) {
      byOrdinal[value.ordinal()] = value;
    }
    return byOrdinal;
  }

  /**
   * A utility method for getting a PropagationMode given that we know its
   * ordinal value. Developers are not expected to use this function. It is
   * mostly used by WireAdapters.
   * @param ordinal
   * @return The propagation mode that has the specified ordinal value.
   */
  public static PropagationMode fromOrdinal(int ordinal) {
    return byOrdinal[ordinal];
  }

  /**
   * @return The default set of propagation modes: THREAD, RMI, JMS_QUEUE, SOAP and MIME_HEADER
   */
  public static EnumSet<PropagationMode> defaultSet() {
    return EnumSet.of(THREAD, RMI, JMS_QUEUE, SOAP, MIME_HEADER);
  }

  public static EnumSet<PropagationMode> defaultSetOneway() {
    return EnumSet.of(THREAD, RMI, JMS_QUEUE, SOAP, MIME_HEADER, ONEWAY);
  }


}
