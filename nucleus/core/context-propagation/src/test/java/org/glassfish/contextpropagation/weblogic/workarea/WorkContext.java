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


/**
 * <code>WorkContext</code> is a marker interface used for marshaling
 * and unmarshaling user data in a <code>WorkArea</code>. The
 * interfaces {@link WorkContextOutput} and
 * {@link WorkContextInput} will only allow primtive types and
 * objects implementing <code>WorkContext</code> to be marshaled. This
 * limits the type surface area that needs to be dealt with by
 * underlying protocols. <code>WorkContext</code> is analogous to
 * {@link java.io.Externalizable} but with some restrictions on the types
 * that can be marshaled. Advanced {@link java.io.Externalizable}
 * features, such as enveloping, are not supported - implementations
 * should provide their own versioning scheme if
 * necessary. <code>WorkContext</code> implementations must provide a
 * public no-arg constructor.
 *
 */
public interface WorkContext {
  /**
   * Writes the implementation of <code>WorkContext</code> to the
   * {@link WorkContextOutput} data stream.
   */
  public void writeContext(WorkContextOutput out) throws IOException;

  /**
   * Reads the implementation of <code>WorkContext</code> from the
   * {@link WorkContextInput} data stream.
   */
  public void readContext(WorkContextInput in) throws IOException;
}
