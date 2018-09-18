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

import java.io.DataOutput;
import java.io.IOException;


/**
 * <code>WorkConectOutput</code> is a primitive stream used for
 * marshaling {@link WorkContext} implementations. It is
 * necessary to limit the types that can be marshaled as part of a
 * <code>WorkArea</code> so that efficient representations can be
 * implemented in a variety of protocols. This representation can also
 * be transparent, enabling runtime filtering in SOAP and other
 * protocols.
 *
 * @see org.glassfish.contextpropagation.weblogic.workarea.WorkContextInput
 */
public interface WorkContextOutput extends DataOutput {
  /**
   * Writes an 8-bit, variable-length, string to the underlying data
   * stream. This is analgous to {@link DataOutput#writeBytes} but the
   * length of the string is also encoded.
   */
  public void writeASCII(String s) throws IOException;

  /**
   * Writes the implementation of {@link WorkContext} to the
   * underlying data stream. The actual class is encoded in the stream
   * so that remote java implementations can decode it.
   */
  public void writeContext(WorkContext ctx) throws IOException;
}
