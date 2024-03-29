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
 *  AutoPersistentNew.java    March 10, 2000    Steffi Rauschenbach
 */

package com.sun.jdo.spi.persistence.support.sqlstore.state;

import com.sun.jdo.spi.persistence.support.sqlstore.ActionDesc;

public class AutoPersistentNew extends LifeCycleState {

    public AutoPersistentNew() {
        // these flags are set only in the constructor
        // and shouldn't be changed afterwards
        // (cannot make them final since they are declared in superclass
        // but their values are specific to subclasses)
        isPersistent = true;
        isPersistentInDataStore = false;
        isAutoPersistent = true;
        isTransactional = true;
        isDirty = true;
        isNew = true;
        isDeleted = false;
        isNavigable = false;
        isRefreshable = false;
        isBeforeImageUpdatable = false;
        needsRegister = true;
        needsReload = false;
        needsRestoreOnRollback = true;
        updateAction = ActionDesc.LOG_CREATE;

        stateType = AP_NEW;
    }

    public LifeCycleState transitionMakePersistent() {
        return changeState(P_NEW);
    }

    public LifeCycleState transitionDeletePersistent() {
        return changeState(AP_NEW_DELETED);
    }

    public LifeCycleState transitionFlushed() {
        return changeState(AP_NEW_FLUSHED);
    }

    public LifeCycleState transitionCommit(boolean retainValues) {
        return changeState(TRANSIENT);
    }

    public LifeCycleState transitionRollback(boolean retainValues) {
        return changeState(TRANSIENT);
    }

    public LifeCycleState transitionMakePending() {
        return changeState(AP_NEW_PENDING);
    }

    public boolean needsRestoreOnRollback(boolean retainValues) {
        //
        // This is a special case where retores doesn't depend on
        // retainValues.
        //
        return needsRestoreOnRollback;
    }
}










