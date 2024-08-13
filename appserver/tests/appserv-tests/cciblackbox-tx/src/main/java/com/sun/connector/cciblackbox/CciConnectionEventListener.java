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

package com.sun.connector.cciblackbox;

import jakarta.resource.spi.ConnectionEvent;
import jakarta.resource.spi.ConnectionEventListener;
import jakarta.resource.spi.ManagedConnection;

import java.util.Vector;

/**
 * The connector architecture provides an event callback mechanism that
 * enables an application server to receive notifications from a
 * ManagedConnection instance. The App server implements this class in order to
 * listen to event notifications from ManagedConnection instance.
 *
 * @author Sheetal Vartak
 */
public class CciConnectionEventListener implements javax.sql.ConnectionEventListener {

  private Vector listeners;

  private ManagedConnection mcon;

  public CciConnectionEventListener(ManagedConnection mcon) {
    listeners = new Vector();
    this.mcon = mcon;
  }

  public void sendEvent(int eventType, Exception ex, Object connectionHandle) {
    Vector list = (Vector) listeners.clone();
    ConnectionEvent ce = null;
    if (ex == null) {
      ce = new ConnectionEvent(mcon, eventType);
    } else {
      ce = new ConnectionEvent(mcon, eventType, ex);
    }
    if (connectionHandle != null) {
      ce.setConnectionHandle(connectionHandle);
    }
    int size = list.size();
    for (int i = 0; i < size; i++) {
      ConnectionEventListener l = (ConnectionEventListener) list.elementAt(i);
      switch (eventType) {
      case ConnectionEvent.CONNECTION_CLOSED:
        l.connectionClosed(ce);
        break;
      case ConnectionEvent.LOCAL_TRANSACTION_STARTED:
        l.localTransactionStarted(ce);
        break;
      case ConnectionEvent.LOCAL_TRANSACTION_COMMITTED:
        l.localTransactionCommitted(ce);
        break;
      case ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK:
        l.localTransactionRolledback(ce);
        break;
      case ConnectionEvent.CONNECTION_ERROR_OCCURRED:
        l.connectionErrorOccurred(ce);
        break;
      default:
        throw new IllegalArgumentException("Illegal eventType: " + eventType);
      }
    }
  }

  public void addConnectorListener(ConnectionEventListener l) {
    listeners.addElement(l);
  }

  public void removeConnectorListener(ConnectionEventListener l) {
    listeners.removeElement(l);
  }

  public void connectionClosed(javax.sql.ConnectionEvent event) {
    // do nothing. The event is sent by the CciConnection wrapper
  }

  public void connectionErrorOccurred(javax.sql.ConnectionEvent event) {
    sendEvent(ConnectionEvent.CONNECTION_ERROR_OCCURRED, event.getSQLException(), null);
  }
}
