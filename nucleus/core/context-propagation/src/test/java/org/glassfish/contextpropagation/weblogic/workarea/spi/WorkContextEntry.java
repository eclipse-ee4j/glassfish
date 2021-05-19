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

package org.glassfish.contextpropagation.weblogic.workarea.spi;

import java.io.IOException;

import org.glassfish.contextpropagation.weblogic.workarea.WorkContext;
import org.glassfish.contextpropagation.weblogic.workarea.WorkContextOutput;



/**
 * <code>WorkContextEntry</code> encapsulates the runtime state of a
 * WorkArea property so that it can be marshaled transparently by an
 * underlying protocol.
 * @exclude
 */
public interface WorkContextEntry
{
  public static final WorkContextEntry
    NULL_CONTEXT = new WorkContextEntryImpl(null, null, 1);

  public WorkContext getWorkContext();
  public String getName();
  public int getPropagationMode();
  public boolean isOriginator();

  /**
   * Writes the implementation of {@link org.glassfish.contextpropagation.weblogic.workarea.WorkContext} to the
   * {@link org.glassfish.contextpropagation.weblogic.workarea.WorkContextOutput} data stream.
   */
  public void write(WorkContextOutput out) throws IOException;
}
