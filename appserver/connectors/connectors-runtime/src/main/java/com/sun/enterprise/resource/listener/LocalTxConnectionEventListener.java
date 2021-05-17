/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.resource.listener;

import com.sun.enterprise.resource.pool.PoolManager;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.resource.*;

import jakarta.resource.spi.*;
import java.util.*;

/**
 * @author Binod P.G
 */
public class LocalTxConnectionEventListener extends ConnectionEventListener {

    private PoolManager poolMgr;

    // connectionHandle -> ResourceHandle
    // Whenever a connection is associated with a ManagedConnection,
    // that connection and the resourcehandle associated with its
    // original ManagedConnection will be put in this table.
    private IdentityHashMap associatedHandles;

    private ResourceHandle resource;

    public LocalTxConnectionEventListener(ResourceHandle resource) {
        this.resource = resource;
        this.associatedHandles = new IdentityHashMap(10);
        this.poolMgr = ConnectorRuntime.getRuntime().getPoolManager();
    }

    public void connectionClosed(ConnectionEvent evt) {
        Object connectionHandle = evt.getConnectionHandle();
        ResourceHandle handle = resource;
        if (associatedHandles.containsKey(connectionHandle)) {
            handle = (ResourceHandle) associatedHandles.get(connectionHandle);
        }
        poolMgr.resourceClosed(handle);
    }

    public void connectionErrorOccurred(ConnectionEvent evt) {
        resource.setConnectionErrorOccurred();
        ManagedConnection mc = (ManagedConnection) evt.getSource();
        mc.removeConnectionEventListener(this);
        poolMgr.resourceErrorOccurred( resource );
/*
        try {
            mc.destroy();
        } catch (Exception ex) {
            // ignore exception
        }
*/
    }

    /**
     * Resource adapters will signal that the connection being closed is bad.
     * @param evt ConnectionEvent
     */
    public void badConnectionClosed(ConnectionEvent evt){
        Object connectionHandle = evt.getConnectionHandle();
        ResourceHandle handle = resource;
        if (associatedHandles.containsKey(connectionHandle)) {
            handle = (ResourceHandle) associatedHandles.get(connectionHandle);
        }
        ManagedConnection mc = (ManagedConnection) evt.getSource();
        mc.removeConnectionEventListener(this);

        poolMgr.badResourceClosed(handle);
    }

    public void localTransactionStarted(ConnectionEvent evt) {
            // no-op
    }

    public void localTransactionCommitted(ConnectionEvent evt) {
         // no-op
    }

    public void localTransactionRolledback(ConnectionEvent evt) {
        // no-op
    }

    public void associateHandle(Object c, ResourceHandle h) {
        associatedHandles.put(c, h);
    }

    public ResourceHandle  removeAssociation(Object c) {
        return (ResourceHandle) associatedHandles.remove(c);
    }

    public Map getAssociatedHandles(){
        return associatedHandles;
    }

}

