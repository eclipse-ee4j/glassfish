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
// Module:      RegisteredSyncs.java
//
// Description: Synchronization participant management.
//
// Product:     com.sun.jts.CosTransactions
//
// Author:      Simon Holdsworth
//
// Date:        March, 1997
//----------------------------------------------------------------------------

package com.sun.jts.CosTransactions;

import com.sun.logging.LogDomains;

import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CosTransactions.Status;
import org.omg.CosTransactions.Synchronization;

/**
 * The RegisteredSyncs class provides operations that manage a set of
 * Synchronization objects involved in a transaction. In order to avoid
 * sending multiple synchronization requests to the same resource we require
 * some way to perform Synchronization reference comparisons.
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
//----------------------------------------------------------------------------

class RegisteredSyncs {

    private Vector registered = new Vector();

    /*
        Logger to log transaction messages
    */
    static Logger _logger = LogDomains.getLogger(RegisteredSyncs.class, LogDomains.TRANSACTION_LOGGER);

    /**
     * Default RegisteredSyncs constructor.
     *
     * @param
     *
     * @return
     *
     * @see
     */
    RegisteredSyncs() {}

    /**
     * Distributes before completion operations to all registered
     * Synchronization objects.
     * <p>
     * Returns a boolean to indicate success/failure.
     *
     * @param
     *
     * @return  Indicates success of the operation.
     *
     * @see
     */
    boolean distributeBefore() {

        boolean result = true;

        for (int i = 0; i < registered.size() && result == true; i++) {
            Synchronization sync = (Synchronization) registered.elementAt(i);
            try {
                if (_logger.isLoggable(Level.FINEST)) {
                    _logger.logp(Level.FINEST, "RegisterdSyncs", "distributeBefore()",
                        "Before invoking before_completion() on synchronization object " + sync);
                }

                sync.before_completion();

                if (_logger.isLoggable(Level.FINEST)) {
                    _logger.logp(Level.FINEST, "RegisterdSyncs", "distributeBefore()",
                        "After invoking before_completion() on synchronization object " + sync);
                }
            } catch (RuntimeException rex) {
                // Exception was logged in SynchronizationImpl
                throw rex;
            } catch (Throwable exc) {
                _logger.log(Level.WARNING, "jts.exception_in_synchronization_operation",
                        new java.lang.Object[] { exc.toString(),"before_completion"});
                result = false;
            }
        }

        return result;
    }

    /**
     * Distributes after completion operations to all registered
     * Synchronization objects.
     *
     * @param status  Indicates whether the transaction committed.
     *
     * @return
     *
     * @see
     */
    void distributeAfter(Status status) {

        for (int i = 0; i < registered.size(); i++) {
            boolean isProxy = false;
            Synchronization sync = (Synchronization) registered.elementAt(i);

            // COMMENT(Ram J) the instanceof operation should be replaced
            // by a is_local() call, once the local object contract is
            // implemented.
            if (!(sync instanceof com.sun.jts.jta.SynchronizationImpl)) {
                isProxy = Configuration.getProxyChecker().isProxy(sync);
            }

            try {
                if (_logger.isLoggable(Level.FINEST)) {
                    _logger.logp(Level.FINEST, "RegisterdSyncs", "distributeAfter()",
                        "Before invoking after_completion() on synchronization object " + sync);
                }

                sync.after_completion(status);

                if (_logger.isLoggable(Level.FINEST)) {
                    _logger.logp(Level.FINEST, "RegisterdSyncs", "distributeAfter()",
                        "After invoking after_completion() on" + "synchronization object" + sync);
                }
            } catch (Throwable exc) {
                // Discard any exceptions at this point.
                if (exc instanceof OBJECT_NOT_EXIST ||
                        exc instanceof COMM_FAILURE) {
                    // ignore i.e., no need to log this error (Ram J)
                    // this can happen normally during after_completion flow,
                    // since remote sync objects would go away when the
                    // subordinate cleans up (i.e, the subordinate would have
                    // called afterCompletions locally before going away).
                } else {
                    _logger.log(Level.WARNING,
                            "jts.exception_in_synchronization_operation",
                            new java.lang.Object[] { exc.toString(),
                            "after_completion"});
                }
            }

            // Release the object if it is a proxy.
            if (isProxy) {
                sync._release();
            }
        }
    }

    /**
     * Adds a reference to a Synchronization object to the set.
     * <p>
     * If there is no such set then a new one is created with the single
     * Synchronization reference.
     *
     * @param obj  The Synchronization object to be added.
     *
     * @return
     *
     * @see
     */
    void addSync(Synchronization obj) {
        registered.addElement(obj);
    }

    /**
     * Empties the set of registered Synchronization objects.
     *
     * @param
     *
     * @return
     *
     * @see
     */
    void empty() {
        registered.removeAllElements();
    }

    /**
     * Checks whether there are any Synchronization objects registered.
     * <p>
     * If there are, the operation returns true, otherwise false.
     *
     * @param
     *
     * @return  Indicates whether any objects are registered.
     *
     * @see
     */
    boolean involved() {

        boolean result = (registered.size() != 0);
        return result;
    }
}
