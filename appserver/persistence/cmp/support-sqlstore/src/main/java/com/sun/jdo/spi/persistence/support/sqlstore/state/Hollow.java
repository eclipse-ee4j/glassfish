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

/*
 *  Hollow.java    March 10, 2000    Steffi Rauschenbach
 */

package com.sun.jdo.spi.persistence.support.sqlstore.state;

import com.sun.jdo.spi.persistence.support.sqlstore.ActionDesc;

public class Hollow extends LifeCycleState {

    public Hollow() {
        // these flags are set only in the constructor
        // and shouldn't be changed afterwards
        // (cannot make them final since they are declared in superclass
        // but their values are specific to subclasses)
        isPersistent = true;
        isPersistentInDataStore = true;
        isTransactional = false;
        isDirty = false;
        isNew = false;
        isDeleted = false;
        isBeforeImageUpdatable = false;
        isRefreshable = false;
        needsRegister = false;
        needsReload = true;
        needsRestoreOnRollback = false;
        updateAction = ActionDesc.LOG_NOOP;

        stateType = HOLLOW;
    }

    /**
     * Operations that cause life cycle state transitions
     */
    public LifeCycleState transitionDeletePersistent() {
        return changeState(P_DELETED);
    }

    public LifeCycleState transitionReadField(boolean optimistic, boolean nontransactionalRead,
                                              boolean transactionActive) {
        if (!nontransactionalRead) {
            assertTransaction(transactionActive);
        }

        if (optimistic || (nontransactionalRead && !transactionActive)) {
            return changeState(P_NON_TX);
        } else {
            return changeState(P_CLEAN);
        }
    }

    public LifeCycleState transitionWriteField(boolean transactionActive) {
        assertTransaction(transactionActive);
        return changeState(P_DIRTY);
    }

    public boolean needsReload(boolean optimistic,
                               boolean nontransactionalRead,
                               boolean transactionActive) {
        //
        // Don't allow reload in the transaction is not active
        // and nontransactionalRead is false.
        //
        if (!transactionActive && !nontransactionalRead) {
            return false;
        }

        return true;
    }

}









