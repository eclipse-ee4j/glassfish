/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jts.jtsxa;

import com.sun.jts.CosTransactions.Configuration;
import com.sun.jts.codegen.otsidl.JCoordinatorHelper;

import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.Coordinator;
import org.omg.CosTransactions.otid_t;

/**
 * This is an Utility class containing helper functions.
 */
public class Utility {

    // static variables

    private static org.omg.CosTransactions.Current current = null;

    /*
     * All Utility methods are static.
     * It is not possible to create a JTSXA instance variable.
     */
    private Utility() {}

    /**
     * Obtain the current Control object.
     *
     * @return the current control object, or null if the Control cannot be obtained.
     * @see org.omg.CosTransactions.Control
     */
    public static Control getControl() {
        Control control = null;

        try {
            if (current == null) {
                current = (org.omg.CosTransactions.Current) Configuration.
                    getORB().resolve_initial_references("TransactionCurrent"/*#Frozen*/);
            }
            control = current.get_control();
        } catch(Exception e) {
            // empty
        }

        return control;
    }

    /**
     * Obtain the coordinator object from the supplied control.
     * <p>If a null control is supplied, an null coordinator will be returned.
     *
     * @param control the control object for which the coordinator
     *        will be returned
     *
     * @return the coordinator, or null if no coordinator can be obtained.
     *
     * @see org.omg.CosTransactions.Control
     * @see org.omg.CosTransactions.Coordinator
     */
    public static Coordinator getCoordinator(Control control) {
        Coordinator coordinator = null;

        if (control == null) {
            return null;
        }

        try {
            coordinator = control.get_coordinator();
        } catch(Exception e) {
            coordinator = null;
        }

        return coordinator;
    }

    /**
     * Obtain the global transaction identifier for the supplied coordinator.
     *
     * @param coordinator the coordinator representing the transaction for which
     *                    the global transaction identifier is required
     *
     * @return the global transaction identifier.
     *
     * @see com.sun.jts.jtsxa.XID
     */
    public static XID getXID(Coordinator coordinator) {
        otid_t tid = null;
        XID xid = new XID();

        if (coordinator == null) {
            return null;
        }

        try {
            tid = JCoordinatorHelper.narrow(coordinator).getGlobalTID();
            xid.copy(tid);
        } catch(Exception e) {
            return null;
        }

        return xid;
    }

    /**
     * Obtain the global transaction identifier for the current transaction.
     *
     * @return the global transaction identifier.
     *
     * @see com.sun.jts.jtsxa.XID
     */
    public static XID getXID() {
        Control control = null;
        Coordinator coordinator = null;

        control = getControl();
        coordinator = getCoordinator(control);

        XID xid = getXID(coordinator);

        return xid;
    }
}
