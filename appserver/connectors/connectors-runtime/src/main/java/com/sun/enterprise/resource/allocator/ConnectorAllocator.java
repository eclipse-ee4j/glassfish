/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.resource.allocator;

import com.sun.appserv.connectors.internal.api.PoolingException;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.resource.ClientSecurityInfo;
import com.sun.enterprise.resource.ResourceHandle;
import com.sun.enterprise.resource.ResourceSpec;
import com.sun.enterprise.resource.pool.PoolManager;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionEvent;
import jakarta.resource.spi.ConnectionEventListener;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionFactory;

import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;


/**
 * @author Tony Ng
 */
public class ConnectorAllocator extends AbstractConnectorAllocator {

    private final boolean shareable;


    class ConnectionListenerImpl extends com.sun.enterprise.resource.listener.ConnectionEventListener {
        private final ResourceHandle resource;

        ConnectionListenerImpl(ResourceHandle resource) {
            this.resource = resource;
        }

        @Override
        public void connectionClosed(ConnectionEvent evt) {
            if (resource.hasConnectionErrorOccurred()) {
                return;
            }
            resource.decrementCount();
            if (resource.getShareCount() == 0) {
                poolMgr.resourceClosed(resource);
            }
        }

        /**
         * Resource adapters will signal that the connection being closed is bad.
         *
         * @param evt ConnectionEvent
         */
        @Override
        public void badConnectionClosed(ConnectionEvent evt) {

            if (resource.hasConnectionErrorOccurred()) {
                return;
            }
            resource.decrementCount();
            if (resource.getShareCount() == 0) {
                ManagedConnection mc = (ManagedConnection) evt.getSource();
                mc.removeConnectionEventListener(this);
                poolMgr.badResourceClosed(resource);
            }
        }

        /**
         * Resource adapters will signal that the connection is being aborted.
         *
         * @param evt ConnectionEvent
         */
        @Override
        public void connectionAbortOccurred(ConnectionEvent evt) {
            resource.setConnectionErrorOccurred();

            ManagedConnection mc = (ManagedConnection) evt.getSource();
            mc.removeConnectionEventListener(this);
            poolMgr.resourceAbortOccurred(resource);
        }

        @Override
        public void connectionErrorOccurred(ConnectionEvent evt) {
            resource.setConnectionErrorOccurred();

            ManagedConnection mc = (ManagedConnection) evt.getSource();
            mc.removeConnectionEventListener(this);
            poolMgr.resourceErrorOccurred(resource);
/*
            try {
                mc.destroy();
            } catch (Exception ex) {
                // ignore exception
            }
*/
        }

        @Override
        public void localTransactionStarted(ConnectionEvent evt) {
            // no-op
        }

        @Override
        public void localTransactionCommitted(ConnectionEvent evt) {
            // no-op
        }

        @Override
        public void localTransactionRolledback(ConnectionEvent evt) {
            // no-op
        }
    }

    public ConnectorAllocator(PoolManager poolMgr,
                              ManagedConnectionFactory mcf,
                              ResourceSpec spec,
                              Subject subject,
                              ConnectionRequestInfo reqInfo,
                              ClientSecurityInfo info,
                              ConnectorDescriptor desc,
                              boolean shareable) {
        super(poolMgr, mcf, spec, subject, reqInfo, info, desc);
        this.shareable = shareable;
    }


    @Override
    public ResourceHandle createResource() throws PoolingException {
        try {
            ManagedConnection mc = mcf.createManagedConnection(subject, reqInfo);
            ResourceHandle resource = createResourceHandle(mc, spec, this, info);
            ConnectionEventListener l = new ConnectionListenerImpl(resource);
            mc.addConnectionEventListener(l);
            return resource;
        } catch (ResourceException ex) {
            throw new PoolingException(ex);
        }
    }

    @Override
    public void fillInResourceObjects(ResourceHandle resource)
            throws PoolingException {
        try {
            ManagedConnection mc = (ManagedConnection) resource.getResource();
            Object con = mc.getConnection(subject, reqInfo);
            resource.incrementCount();
            XAResource xares = mc.getXAResource();
            resource.fillInResourceObjects(con, xares);
        } catch (ResourceException ex) {
            throw new PoolingException(ex);
        }
    }

    @Override
    public void destroyResource(ResourceHandle resource)
            throws PoolingException {

        try {
            closeUserConnection(resource);
        } catch (Exception ex) {
            // ignore error
        }

        try {
            ManagedConnection mc = (ManagedConnection) resource.getResource();
            mc.destroy();
        } catch (Exception ex) {
            throw new PoolingException(ex);
        }

    }

    @Override
    public boolean shareableWithinComponent() {
        return shareable;
    }
}
