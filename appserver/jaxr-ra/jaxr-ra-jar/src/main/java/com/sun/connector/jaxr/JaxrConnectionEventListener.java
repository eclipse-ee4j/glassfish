/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.connector.jaxr;

import java.util.ResourceBundle;
import java.util.Vector;
import java.util.logging.Logger;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ManagedConnection;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;

public class JaxrConnectionEventListener
{
  private Vector listeners = new Vector();
  private ManagedConnection mcon;
  @LogMessagesResourceBundle
  private static final Logger log = Logger.getLogger("com.sun.connector.jaxr");
  
  public JaxrConnectionEventListener(ManagedConnection paramManagedConnection)
  {
    this.log.fine("JAXRConnectionEventListener constructor - ManagedConnection as parameter");
    this.mcon = paramManagedConnection;
  }
  
  public void sendEvent(int paramInt, Exception paramException, Object paramObject)
  {
    Vector localVector = (Vector)this.listeners.clone();
    ConnectionEvent localConnectionEvent = null;
    this.log.fine("JAXRConnectionEventListener sendEvent creating connection Event");
    if (paramException == null) {
      localConnectionEvent = new ConnectionEvent(this.mcon, paramInt);
    } else {
      localConnectionEvent = new ConnectionEvent(this.mcon, paramInt, paramException);
    }
    if (paramObject != null)
    {
      this.log.fine("JAXRConnectionEventListener sendEvent setting connection handle on connection Event");
      localConnectionEvent.setConnectionHandle(paramObject);
    }
    int i = localVector.size();
    for (int j = 0; j < i; j++)
    {
      ConnectionEventListener localConnectionEventListener = (ConnectionEventListener)localVector.elementAt(j);
      this.log.fine("JAXRConnectionEventListener sendEvent processing eventType connection Event");
      switch (paramInt)
      {
      case 1: 
        localConnectionEventListener.connectionClosed(localConnectionEvent);
        this.log.fine("JAXRConnectionEventListener sendEvent processing Closed eventType --calling listener.closed");
        break;
      case 2: 
        localConnectionEventListener.localTransactionStarted(localConnectionEvent);
        break;
      case 3: 
        localConnectionEventListener.localTransactionCommitted(localConnectionEvent);
        break;
      case 4: 
        localConnectionEventListener.localTransactionRolledback(localConnectionEvent);
        break;
      case 5: 
        localConnectionEventListener.connectionErrorOccurred(localConnectionEvent);
        break;
      default: 
        throw new IllegalArgumentException(ResourceBundle.getBundle("com/sun/connector/jaxr/LocalStrings").getString("Illegal_eventType:_") + paramInt);
      }
    }
  }
  
  public void addConnectorListener(ConnectionEventListener paramConnectionEventListener)
  {
    this.listeners.addElement(paramConnectionEventListener);
  }
  
  public void removeConnectorListener(ConnectionEventListener paramConnectionEventListener)
  {
    this.listeners.removeElement(paramConnectionEventListener);
  }
  
  public void connectionClosed(ConnectionEvent paramConnectionEvent)
  {
    this.log.fine("JAXRConnectionEventListener connectionClosed - doing nothing");
  }
  
  public void connectionErrorOccurred(ConnectionEvent paramConnectionEvent)
  {
    sendEvent(5, null, null);
  }
}

