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
 * An implementation for propagating simple string-based {@link
 * WorkContext}s.
 */
@SuppressWarnings("serial")
public class StringWorkContext implements PrimitiveWorkContext, Serializable
{
  private String str;
  
  public StringWorkContext() {
  }
  
  /* package */ StringWorkContext(String str) {
    this.str = str;
  }

  public String toString() { return str; }
  public Object get() { return str; }

  public boolean equals(Object obj) {
    if (obj instanceof StringWorkContext) {
      return ((StringWorkContext)obj).str.equals(str);
    }
    return false;
  }
  
  public void writeContext(WorkContextOutput out) throws IOException {
    out.writeUTF(str);
  }
  
  public void readContext(WorkContextInput in) throws IOException {
    str = in.readUTF();
  }
}
