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

package com.sun.enterprise.resource.rm;

import jakarta.transaction.*;
import jakarta.resource.spi.ManagedConnection;
import java.util.logging.Level;
import java.util.List;

import jakarta.resource.ResourceException;

import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.transaction.api.JavaEETransactionManager;
import com.sun.enterprise.resource.ResourceHandle;
import com.sun.enterprise.resource.pool.PoolManager;

import com.sun.appserv.connectors.internal.api.PoolingException;

import java.util.ListIterator;

import org.glassfish.api.invocation.ComponentInvocation;

/**
 * This class is used for lazy enlistment of a resource
 *
 * @author Aditya Gore
 */
public class LazyEnlistableResourceManagerImpl extends ResourceManagerImpl {


    protected void enlist( JavaEETransactionManager tm, Transaction tran,
        ResourceHandle h ){
        //do nothing
    }

    /**
     * Overridden to suspend lazyenlistment.
     * @param handle
     * @throws PoolingException
     */
      public void registerResource(ResourceHandle handle)
            throws PoolingException {
            handle.setEnlistmentSuspended(true);
            super.registerResource(handle);
     }
    /**
     * This is called by the PoolManager (in turn by the LazyEnlistableConnectionManager)
     * when a lazy enlistment is sought.
     * @param mc ManagedConnection
     * @throws ResourceException
     */
    public void lazyEnlist( ManagedConnection mc ) throws ResourceException {
        if ( _logger.isLoggable(Level.FINE) ) {
            _logger.fine("Entering lazyEnlist");
        }

        //J2EETransactionManager tm = Switch.getSwitch().getTransactionManager();
        JavaEETransactionManager tm = ConnectorRuntime.getRuntime().getTransactionManager();
        Transaction tran = null;

        try {
            tran = tm.getTransaction();
            if ( tran == null ) {
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.fine(" Transaction null - not enlisting ");
                }

                return;
            }
        } catch( SystemException se ) {
            ResourceException re = new ResourceException( se.getMessage() );
            re.initCause( se );
            throw re;
        }

        //List invList = Switch.getSwitch().getInvocationManager().getAllInvocations();
        List invList = ConnectorRuntime.getRuntime().getInvocationManager().getAllInvocations();

        ResourceHandle h = null;
        for ( int j = invList.size(); j > 0; j-- ) {
            ComponentInvocation inv = (ComponentInvocation) invList.get( j - 1 );
            Object comp = inv.getInstance();

            List l = tm.getResourceList( comp, inv );

            ListIterator it = l.listIterator();
            while( it.hasNext()) {
                ResourceHandle hand = (ResourceHandle) it.next();
                ManagedConnection toEnlist = (ManagedConnection) hand.getResource();
                if ( mc.equals( toEnlist ) ) {
                    h = hand;
                    break;
                }
            }
        }

        //NOTE: Notice that here we are always assuming that the connection we
        //are trying to enlist was acquired in this component only. This
        //might be inadequate in situations where component A acquires a connection
        //and passes it on to a method of component B, and the lazyEnlist is
        //triggered in B
        //At this point however, we will only support the straight and narrow
        //case where a connection is acquired and then used in the same component.
        //The other case might or might not work
        if( h != null && h.getResourceState().isUnenlisted()) {
            try {
                //Enable the suspended lazyenlistment so as to enlist the resource.
                    h.setEnlistmentSuspended(false);
                    tm.enlistResource( tran, h );
                //Suspend it back
                    h.setEnlistmentSuspended(true);
            } catch( Exception e ) {
                //In the rare cases where enlistResource throws exception, we
                //should return the resource to the pool
                    PoolManager mgr = ConnectorRuntime.getRuntime().getPoolManager();
                    mgr.putbackDirectToPool( h, h.getResourceSpec().getPoolInfo());
                    _logger.log(Level.WARNING,
                                "poolmgr.err_enlisting_res_in_getconn", h
                                .getResourceSpec().getPoolInfo());
                if (_logger.isLoggable(Level.FINE) ) {
                    _logger.fine("rm.enlistResource threw Exception. Returning resource to pool");
                }
                //and rethrow the exception
                throw new ResourceException( e );
            }
        }
    }

}
