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

import org.glassfish.contextpropagation.adaptors.MockLoggerAdapter;
import org.glassfish.contextpropagation.weblogic.workarea.WorkContext;
import org.glassfish.contextpropagation.weblogic.workarea.WorkContextInput;
import org.glassfish.contextpropagation.weblogic.workarea.WorkContextOutput;



/**
 * A basic implementation of <code>WorkAreaContext</code> 
 * @exclude
 */
public final class WorkContextEntryImpl implements WorkContextEntry
{
  public static final String[] PROP_NAMES = new String[] {
    "LOCAL",
    "WORK",
    "RMI",
    "TRANSACTION",
    "JMS_QUEUE",
    "JMS_TOPIC",
    "SOAP",
    "MIME_HEADER",
    "ONEWAY"
  };

  private String name;
  private int propagationMode;
  private WorkContext context;
  private boolean originator;

  @SuppressWarnings("unused")
  private WorkContextEntryImpl() { }
  
  public WorkContextEntryImpl(String name, WorkContext context,
                              int propagationMode) {
    this.name = name;
    this.context = context;
    this.propagationMode = propagationMode;
    this.originator = true;
  }

  private WorkContextEntryImpl(String name, WorkContextInput in)
    throws IOException, ClassNotFoundException 
  {
    this.name = name;
    propagationMode = in.readInt();
    context = in.readContext();
  }
  
  public WorkContext getWorkContext() {
    return context;
  }
  
  public int hashCode() {
    return name.hashCode();
  }

  public boolean equals(Object obj) {
    if (obj instanceof WorkContextEntry) {
      return ((WorkContextEntry)obj).getName().equals(name);
    }
    return false;
  }
  
  public String getName() {
    return name;
  }

  public int getPropagationMode() {
    return propagationMode;
  }

  public boolean isOriginator() {
    return originator;
  }

  public void write(WorkContextOutput out) throws IOException 
  {
    if (this == NULL_CONTEXT) {
      out.writeUTF("");
    }
    else {
      out.writeUTF(name);
      out.writeInt(propagationMode);
      out.writeContext(context);
    }
  }

  public static WorkContextEntry readEntry(WorkContextInput in) 
    throws IOException, ClassNotFoundException 
  {
    String name = in.readUTF();
    MockLoggerAdapter.debug("Read key: " + name);
    if (name.length() == 0) {
      return NULL_CONTEXT;
    }
    else {
      return new WorkContextEntryImpl(name, in);
    }
  }
  
  public String toString() {
    StringBuffer sb = new StringBuffer(name);
    sb.append(", ");
    int p = propagationMode;
    for (int i=0; i<9; i++) {
      if ((p >>>= 1) == 1) {
        sb.append(" | ").append(PROP_NAMES[i]);
      }
    }
    return sb.toString();
  }
}


