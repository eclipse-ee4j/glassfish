/*
 * Copyright (c) 2002, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.connector.blackbox;

import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ManagedConnection;
import java.util.Vector;

/**
 * @author Tony Ng
 */
public class JdbcConnectionEventListener
        implements javax.sql.ConnectionEventListener {

    private Vector listeners;
    private ManagedConnection mcon;

    public JdbcConnectionEventListener(ManagedConnection mcon) {
        listeners = new Vector();
        this.mcon = mcon;
    }

    public void sendEvent(int eventType, Exception ex,
                          Object connectionHandle) {
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
            ConnectionEventListener l =
                    (ConnectionEventListener) list.elementAt(i);
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
                    System.out.println(" Received CONNECTION_ERROR_OCCURRED in listener");
                    break;
                default:
                    throw new IllegalArgumentException("Illegal eventType: " +
                            eventType);
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
        // do nothing. The event is sent by the JdbcConnection wrapper
    }

    public void connectionErrorOccurred(javax.sql.ConnectionEvent event) {
        sendEvent(ConnectionEvent.CONNECTION_ERROR_OCCURRED,
                event.getSQLException(), null);
    }
}
