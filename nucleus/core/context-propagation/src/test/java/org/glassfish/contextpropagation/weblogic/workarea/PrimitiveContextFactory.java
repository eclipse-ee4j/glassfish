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

import java.io.IOException;
import java.io.Serializable;


/**
 * <code>PrimitiveContextFactory</code> provides internal users and
 * layered products convenience mechanisms for creating
 * {@link WorkContext}s instances containing primitive data.
 *
 * @see org.glassfish.contextpropagation.weblogic.workarea.WorkContextMap
 *
 */
public class PrimitiveContextFactory
{
  /**
   * Creates a short {@link WorkContext} key based on
   * <code>key</code>. Short keys are more efficiently serialized. The
   * returned key will always be the same for the same values of
   * <code>key</code>.
   *
   * @see org.glassfish.contextpropagation.weblogic.workarea.WorkContextMap#put
   */
  public static String createEncodedKey(String key) {
    int hash = key.hashCode();
    StringBuffer code = new StringBuffer();
    while (hash != 0) {
      code.append((char)(59 + (hash & 0x3F)));
      hash >>>= 6;
    }
    return code.toString();
  }

  /**
   * Creates a new {@link WorkContext} containing Unicode String
   * context data.
   *
   * @see org.glassfish.contextpropagation.weblogic.workarea.WorkContextMap#put
   */
  public static WorkContext create(String ctx) {
    return new StringWorkContext(ctx);
  }

  /**
   * Creates a new {@link WorkContext} containing 64-bit long
   * context data.
   *
   * @see org.glassfish.contextpropagation.weblogic.workarea.WorkContextMap#put
   */
  public static WorkContext create(long ctx) {
    return new LongWorkContext(ctx);
  }

  /**
   * Creates a new {@link WorkContext} containing 8-bit ASCII
   * context data.
   *
   * @see org.glassfish.contextpropagation.weblogic.workarea.WorkContextMap#put
   */
  public static WorkContext createASCII(String ctx) {
    return new AsciiWorkContext(ctx);
  }

  /**
   * Creates a new {@link WorkContext} containing opaque
   * Serializable context data. <b>CAUTION: use with care</b>. Data
   * propagated in this way will be opaque to underlying protocol
   * implementations and will generally be less efficient.
   *
   * @see org.glassfish.contextpropagation.weblogic.workarea.WorkContextMap#put
   */
  public static WorkContext create(Serializable ctx) throws IOException {
    return new SerializableWorkContext(ctx);
  }

  /**
   * Creates a new {@link WorkContext} containing opaque
   * Serializable context data. The context data is not serialized at the time
   * of creation of WorkContext but only when the WorkContextMap needs to
   * propagate the WorkContext entries. This allows the Serializable context
   * data to be updated even after it is put in the WorkContextMap.
   * <b>CAUTION: use with care</b>. Data
   * propagated in this way will be opaque to underlying protocol
   * implementations and will generally be less efficient.
   *
   * @see org.glassfish.contextpropagation.weblogic.workarea.WorkContextMap#put
   */
  public static WorkContext createMutable(Serializable ctx)
    throws IOException {
    return new SerializableWorkContext(ctx, true /*enableUpdate*/);
  }
}

