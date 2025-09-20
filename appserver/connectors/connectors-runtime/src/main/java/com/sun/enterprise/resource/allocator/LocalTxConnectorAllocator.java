/*
 * Copyright (c) 2021, 2025 Contributors to the Eclipse Foundation
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
import com.sun.enterprise.resource.ConnectorXAResource;
import com.sun.enterprise.resource.ResourceHandle;
import com.sun.enterprise.resource.ResourceSpec;
import com.sun.enterprise.resource.listener.ConnectionEventListener;
import com.sun.enterprise.resource.listener.LocalTxConnectionEventListener;
import com.sun.enterprise.resource.pool.PoolManager;
import com.sun.enterprise.transaction.api.JavaEETransaction;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionFactory;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;

import java.util.logging.Level;

import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

/**
 * LocalTransaction Connector Allocator, used for transaction level:
 * {@link com.sun.appserv.connectors.internal.api.ConnectorConstants#LOCAL_TRANSACTION_INT}.
 *
 * @author Tony Ng
 */
public class LocalTxConnectorAllocator extends AbstractConnectorAllocator {

    private static final String COMMIT = "COMMIT";
    private static final String ROLLBACK = "ROLLBACK";

    private static String transactionCompletionMode = System
        .getProperty("com.sun.enterprise.in-progress-local-transaction.completion-mode");

    private final boolean shareable;


    public LocalTxConnectorAllocator(PoolManager poolMgr,
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

            ResourceHandle resource = createResourceHandle(mc, spec, this);
            ConnectionEventListener l = new LocalTxConnectionEventListener(resource);
            mc.addConnectionEventListener(l);
            resource.setListener(l);

            XAResource xares = new ConnectorXAResource(resource, spec, this, info);
            resource.fillInResourceObjects(null, xares);

            return resource;
        } catch (ResourceException ex) {
            throw new PoolingException(ex);
        }
    }

    @Override
    public void fillInResourceObjects(ResourceHandle resource)
            throws PoolingException {
        try {
            ManagedConnection mc = resource.getResource();
            Object con = mc.getConnection(subject, reqInfo);
            ConnectorXAResource xares = (ConnectorXAResource) resource.getXAResource();
            xares.setUserHandle(con);
            resource.fillInResourceObjects(con, xares);
        } catch (ResourceException ex) {
            throw new PoolingException(ex);
        }
    }

    @Override
    public void destroyResource(ResourceHandle resource)
            throws PoolingException {
        try {
            ManagedConnection mc = resource.getResource();
            XAResource xares = resource.getXAResource();
            forceTransactionCompletion(xares);
            mc.destroy();
            LOG.finest("destroyResource for LocalTxConnectorAllocator done");

        } catch (Exception ex) {
            throw new PoolingException(ex);
        }
    }

    @Override
    public boolean shareableWithinComponent() {
        return shareable;
    }

    private void forceTransactionCompletion(XAResource xares) throws SystemException {
        if(transactionCompletionMode != null){
            if(xares instanceof ConnectorXAResource){
                ConnectorXAResource connectorXARes = (ConnectorXAResource)xares;
                JavaEETransaction j2eetran = connectorXARes.getAssociatedTransaction();
                if(j2eetran != null && j2eetran.isLocalTx()){
                    if(j2eetran.getStatus() == (Status.STATUS_ACTIVE)){
                        try{
                            if(transactionCompletionMode.equalsIgnoreCase(COMMIT)){
                                if(LOG.isLoggable(Level.FINEST)){
                                    LOG.log(Level.FINEST,"Transaction Completion Mode for LocalTx resource is " +
                                            "set as COMMIT, committing transaction");
                                }
                                j2eetran.commit();
                            }else if(transactionCompletionMode.equalsIgnoreCase(ROLLBACK)){
                                if(LOG.isLoggable(Level.FINEST)){
                                    LOG.log(Level.FINEST,"Transaction Completion Mode for LocalTx resource is " +
                                        "set as ROLLBACK, rolling back transaction");
                                }
                                j2eetran.rollback();
                            }else{
                                LOG.log(Level.WARNING,"Unknown transaction completion mode, no action made");
                            }
                        }catch(Exception e){
                            LOG.log(Level.WARNING, "Failure while forcibily completing an incomplete, " +
                                    "local transaction ", e);
                        }
                    }
                }
            }
        }
    }
}
