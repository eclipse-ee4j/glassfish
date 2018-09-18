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

import java.io.DataInput;
import java.io.IOException;



/**
 * <code>WorkContextInput</code> is a primitive stream used for
 * unmarshaling {@link WorkContext} implementations.
 *
 * @see weblogic.workarea.WorkContextOuput
 */
public interface WorkContextInput extends DataInput {
  /**
   * Reads an 8-bit, variable-length, ASCII string from the underlying
   * data stream.
   */
  public String readASCII() throws IOException;

  /**
   * Reads a {@link WorkContext} from the underlying
   * stream. The class is encoded as part of the marshaled form in a
   * protocol-dependent fashion to allow remote java implementations
   * to decode it.
   */
  public WorkContext readContext() throws IOException, ClassNotFoundException;
}
