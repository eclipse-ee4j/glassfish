/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1995-1997 IBM Corp. All rights reserved.
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

//----------------------------------------------------------------------------
//
// Module:      CoordinatorSynchronizationImpl.java
//
// Description: Subordinate Coordinator synchronization interface.
//
// Product:     com.sun.jts.CosTransactions
//
// Author:      Simon Holdsworth
//
// Date:        March, 1997
//----------------------------------------------------------------------------

package com.sun.jts.CosTransactions;

import com.sun.jts.utils.LogFormatter;
import com.sun.logging.LogDomains;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.SystemException;
import org.omg.CosTransactions.Status;
import org.omg.CosTransactions.Synchronization;
import org.omg.CosTransactions.SynchronizationHelper;
import org.omg.CosTransactions.SynchronizationPOA;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive;
import org.omg.PortableServer.POAPackage.ServantNotActive;

/**The CoordinatorSynchronizationImpl interface allows a subordinate Coordinator
 * to be informed of the completion of a transaction, both before the transaction
 * is prepared, and after it is committed or rolled back. Every
 * Synchronization object registered with the subordinate should be called
 * before the operation returns to the superior. An instance of this class
 * should be accessed from only one thread within a process.
 *
 * @version 0.01
 *
 * @author Simon Holdsworth, IBM Corporation
 *
 * @see
*/
//----------------------------------------------------------------------------
// CHANGE HISTORY
//
// Version By     Change Description
//   0.01  SAJH   Initial implementation.
//------------------------------------------------------------------------------

class CoordinatorSynchronizationImpl extends SynchronizationPOA {

    private static POA poa = null;
    private Synchronization thisRef = null;

    private Long           localTID = null;
    private TopCoordinator coordinator = null;
    /*
        Logger to log transaction messages
    */
    static Logger _logger = LogDomains.getLogger(CoordinatorSynchronizationImpl.class, LogDomains.TRANSACTION_LOGGER);

    /**Default CoordinatorSynchronizationImpl constructor.
     *
     * @param
     *
     * @return
     *
     * @see
     */
    CoordinatorSynchronizationImpl() {
    }

    /**Sets up a new CoordinatorSynchronizationImpl object with the Coordinator reference so
     * that it can pass on synchronization requests.
     *
     * @param coord  The Coordinator for the transaction.
     *
     * @return
     *
     * @see
     */
    CoordinatorSynchronizationImpl( TopCoordinator coord ) {

        // Set the instance variables to the values passed in.

        coordinator = coord;
        try {
            localTID = coord.getLocalTID();
        } catch( SystemException exc ) {}

    }

    /**Passes on the before completion operation to the Coordinator.
     *
     * @param
     *
     * @return
     *
     * @exception SystemException  The operation failed.  The minor code provides
     *                             a reason for the failure.
     *
     * @see
     */
    public void before_completion()
        throws SystemException {

        // If there is no Coordinator reference, raise an exception.

        if( coordinator == null ) {
            INTERNAL exc = new INTERNAL(MinorCode.NoCoordinator,
                                        CompletionStatus.COMPLETED_NO);
            throw exc;
        }

        // Pass the before completion operation on to the coordinator.

        coordinator.beforeCompletion();

    }

    /**Passes on the after completion operation to the Coordinator.
     *
     * @param status  The state of the transaction.
     *
     * @return
     *
     * @exception SystemException  The operation failed.  The minor code provides
     *                             a reason for the failure.
     *
     * @see
     */
    public void after_completion( Status status )
        throws SystemException {

        // If there is no Coordinator reference, raise an exception.

        if( coordinator == null ) {
            INTERNAL exc = new INTERNAL(MinorCode.NoCoordinator,
                                        CompletionStatus.COMPLETED_NO);
            throw exc;
        }

        // Pass the after completion operation on to the coordinator.
        // Destroy myself.

        coordinator.afterCompletion(status);
        destroy();

    }

    /**Returns the CORBA Object which represents this object.
     *
     * @param
     *
     * @return  The CORBA object.
     *
     * @see
     */
    Synchronization object() {
        if( poa == null ) poa = Configuration.getPOA("transient"/*#Frozen*/);
        if( thisRef == null ) {
            if( poa == null )
                poa = Configuration.getPOA("transient"/*#Frozen*/);

            try {
                poa.activate_object(this);
                thisRef = SynchronizationHelper.
                            narrow(poa.servant_to_reference(this));
                //thisRef = (Synchronization)this;
            } catch( ServantAlreadyActive saexc ) {
                _logger.log(Level.SEVERE,
                        "jts.create_CoordinatorSynchronization_object_error");
                 String msg = LogFormatter.getLocalizedMessage(_logger,
                               "jts.create_CoordinatorSynchronization_object_error");
                  throw  new org.omg.CORBA.INTERNAL(msg);
            } catch( ServantNotActive snexc ) {
                _logger.log(Level.SEVERE,
                        "jts.create_CoordinatorSynchronization_object_error");
                 String msg = LogFormatter.getLocalizedMessage(_logger,
                               "jts.create_CoordinatorSynchronization_object_error");
                  throw  new org.omg.CORBA.INTERNAL(msg);
            } catch( Exception exc ) {
                _logger.log(Level.SEVERE,
                        "jts.create_CoordinatorSynchronization_object_error");
                 String msg = LogFormatter.getLocalizedMessage(_logger,
                               "jts.create_CoordinatorSynchronization_object_error");
                  throw  new org.omg.CORBA.INTERNAL(msg);
            }
        }

        return thisRef;
    }

    /**Destroys the CoordinatorSynchronizationImpl object.
     *
     * @param
     *
     * @return
     *
     * @see
     */
    void destroy() {
        if( poa != null &&
            thisRef != null )
            try {
                poa.deactivate_object(poa.reference_to_id(thisRef));
                thisRef = null;
            } catch( Exception exc ) {
                 _logger.log(Level.WARNING,"jts.object_destroy_error",
                         "CoordinatorResource");
            }

        coordinator = null;
    }

    /**
     * Returns the CoordinatorSynchronizationImpl which serves the given object.
     *
     * @param  The CORBA Object.
     *
     * @return  The CoordinatorSynchronizationImpl object which serves it.
     *
     * @see
     */
    synchronized static final CoordinatorSynchronizationImpl servant(Synchronization sync) {
        CoordinatorSynchronizationImpl result = null;

        // we will not be able to obtain the
        // servant from our local POA for a proxy sync object.
        // so return null
        if (sync != null && Configuration.getProxyChecker().isProxy(sync)) {
            return result;
        }

        if (sync instanceof CoordinatorSynchronizationImpl ) {
            result = (CoordinatorSynchronizationImpl) sync;
        } else if (poa != null) {
            try {
                result = (CoordinatorSynchronizationImpl) poa.reference_to_servant(sync);
                if( result.thisRef == null )
                    result.thisRef = sync;
            } catch( Exception exc ) {
                _logger.log(Level.WARNING,"jts.cannot_locate_servant",
                            "CoordinatorSynchronization");
            }
        }

        return result;
    }
}
