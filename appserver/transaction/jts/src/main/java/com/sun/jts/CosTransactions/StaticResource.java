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
// Module:      StaticResource.java
//
// Description: Statically-registered Resource interface.
//
// Product:     com.sun.jts.CosTransactions
//
// Author:      Simon Holdsworth
//
// Date:        March, 1997
//----------------------------------------------------------------------------

package com.sun.jts.CosTransactions;

import org.omg.CosTransactions.Coordinator;

/**
 * The StaticResource interface provides operations that allow an object to
 * be informed about changes in transaction associations with threads. The
 * operations are guaranteed to be invoked on the thread on which the
 * association is started or ended. This class is an abstract base class so
 * the behavior here is that which is expected from any subclass.
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

public abstract class StaticResource {

    /**
     * Informs the object that an association has started.
     * <p>
     * That is, a thread association has begun on the calling thread for the
     * transaction represented by the given Coordinator object.
     * A flag is passed indicating whether this association is
     * as a result of a begin operation.
     *
     * @param coord  The transaction whose association is starting.
     * @param begin  Indicates a begin rather than a resume.
     *
     * @return
     *
     * @see
     */
    public abstract void startAssociation(Coordinator coord, boolean begin);

    /**
     * Informs the object that an association has ended.
     * <p>
     * That is, a thread association has ended on the calling thread for the
     * transaction represented by the given Coordinator object.
     * A flag is passed indicating whether this
     * association is as a result of the transaction completing.
     *
     * @param coord     The transaction whose association is starting.
     * @param complete  Indicates a commit/rollback rather than a suspend.
     *
     * @return
     *
     * @see
     */
    public abstract void endAssociation(Coordinator coord, boolean complete);

    /**
     * Registers the StaticResource object.
     * <p>
     * Until this method is called, the StaticResource object will not receive
     * calls to start/endAssociation.
     *
     * @param
     *
     * @return
     *
     * @see
     */
    protected void register() {
        CurrentTransaction.registerStatic(this);
    }
}
