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
// Module:      RegisteredStatics.java
//
// Description: Static Resource management.
//
// Product:     com.sun.jts.CosTransactions
//
// Author:      Simon Holdsworth
//
// Date:        March, 1997
//----------------------------------------------------------------------------

package com.sun.jts.CosTransactions;

import java.util.Vector;

import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CosTransactions.Unavailable;

/**
 * The RegisteredStatics class provides operations that manage the set of
 * StaticResource objects and distributes association operations to those
 * registered objects.
 *
 * @version 0.01
 *
 * @author Simon Holdsworth, IBM Corporation
 *
 * @see
 */

//---------------------------------------------------------------------------
// CHANGE HISTORY
//
// Version By     Change Description
//   0.01  SAJH   Initial implementation.
//---------------------------------------------------------------------------

class RegisteredStatics {

    private Vector registered = new Vector();

    /**
     * Default RegisteredStatics constructor.
     *
     * @param
     *
     * @return
     *
     * @see
     */
    RegisteredStatics() {}

    /**
     * Informs all registered objects an association has started.
     * <p>
     * The Control object which represents the transaction is given.
     * <p>
     * A flag is passed indicating whether this association
     * is as a result of a Current.begin operation.
     *
     * @param control The transaction whose association has started.
     * @param begin   Indicates if this is a begin rather than a resume.
     *
     * @return
     *
     * @see
     */
    void distributeStart(ControlImpl control, boolean begin) {

        // Determine the Coordinator for the transaction.

        org.omg.CosTransactions.Coordinator coord = null;

        try {
            coord = control.get_coordinator();
        } catch (Unavailable exc) {}

        // Browse through the set, telling each that association is starting.

        if (coord != null) {

            for (int i = 0; i < registered.size(); i++) {

                StaticResource resource =
                    (StaticResource) registered.elementAt(i);

                try {
                    resource.startAssociation(coord, begin);
                } catch (INVALID_TRANSACTION exc) {
                    // Catch INVALID_TRANSACTION exception, and allow it to
                    // percolate. We need to inform all previously called
                    // StaticResources that the association has ended
                    // immediately.

                    for (int j = i - 1; j >= 0; j--) {
                        ((StaticResource) registered.elementAt(j)).
                            endAssociation(coord, begin);
                    }

                    throw (INVALID_TRANSACTION)exc.fillInStackTrace();
                } catch (Throwable exc) {
                    // discard any other exception
                }
            }
        }
    }

    /**
     * Informs all registered StaticResource objects that a thread association
     * has ended.
     * <p>
     * The Control object representing the transaction is given.
     * <p>
     * A flag is passed indicating whether this association
     * is as a result of the transaction completing.
     *
     * @param control   The transaction whose association has ended.
     * @param complete  Indicates that this is a commit/rollback rather than a
     *                  suspend.
     *
     * @return
     *
     * @see
     */
    void distributeEnd(ControlImpl control, boolean complete) {

        // Determine the Coordinator for the transaction.

        org.omg.CosTransactions.Coordinator coord = null;

        try {
            coord = control.get_coordinator();
        } catch (Unavailable exc) {}

        // Browse through the set, telling each that the association is ending.

        if (coord != null) {
            for (int i = 0; i < registered.size(); i++) {
                StaticResource resource =
                    (StaticResource)registered.elementAt(i);
                try {
                    resource.endAssociation(coord, complete);
                } catch (Throwable e) {
                    // Discard any exception.
                }
            }
        }
    }

    /**
     * Adds the given StaticResource object to the set of those informed of
     * thread association changes.
     * <p>
     * If there is a current thread association, then the added StaticResource
     * is called immediately so that it is aware of the association.
     *
     * @param obj  The StaticResource to be added.
     *
     * @return
     *
     * @see
     */

    void addStatic(StaticResource obj) {

        registered.addElement(obj);

        // Determine whether there is a current association.

        try {
            org.omg.CosTransactions.Coordinator coord =
                CurrentTransaction.getCurrentCoordinator();

            // Tell the StaticResource that the association has started.
            // Pretend that it is a begin association, as the
            // StaticResource has not seen the transaction before.

            if (coord != null) {
                obj.startAssociation(coord, true);
            }
        } catch(Throwable exc) {
            // Discard any exception.
        }
    }

    /**
     * Removes the given StaticResource object from the set of those
     * informed of thread association changes.
     *
     * @param obj  The StaticResource to be removed.
     *
     * @return  Indicates success of the operation.
     *
     * @see
     */
    boolean removeStatic(StaticResource obj) {

        boolean result = registered.removeElement(obj);
        return result;
    }
}
