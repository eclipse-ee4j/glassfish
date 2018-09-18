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
// Module:      CompletionHandler.java
//
// Description: Common interface for transaction completion objects.
//
// Product:     com.sun.jts.CosTransactions
//
// Author:      Simon Holdsworth
//
// Date:        March, 1997
//----------------------------------------------------------------------------

package com.sun.jts.CosTransactions;

//------------------------------------------------------------------------------
// CompletionHandler interface
//------------------------------------------------------------------------------
/**The CompletionHandler interface provides operations that allow an object
 * to be informed when a Coordinator locally completes a transaction.
 * <p>
 * This is to allow the CoordinatorResource and CoordinatorTerm objects
 * for a transaction to be informed when the Coordinator is completed via
 * some path other than normal.
 *
 * @version 0.1
 *
 * @author Simon Holdsworth, IBM Corporation
 *
 * @see
 */
// CHANGE HISTORY
//
// Version By     Change Description
//   0.1   SAJH   Initial implementation.
//------------------------------------------------------------------------------

interface CompletionHandler {

    /**Informs the ComplemtionHandler object that the transaction it represents
     * has completed.
     * <p>
     * Flags indicate whether the transaction aborted, and whether there was
     * heuristic damage.
     * <p>
     * This operation is invoked by a Coordinator when it is rolled back,
     * potentially by a caller other than the CompletionHandler itself.
     *
     * @param aborted          Indicates whether the transaction locally aborted.
     * @param heuristicDamage  Indicates local heuristic damage.
     *
     * @return
     *
     * @see
     */
    abstract void setCompleted( boolean aborted,
                                boolean heuristicDamage );
}
