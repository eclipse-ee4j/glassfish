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
 *  PersistentDeleted.java    March 10, 2000    Steffi Rauschenbach
 */

package com.sun.jdo.spi.persistence.support.sqlstore.state;

import com.sun.jdo.api.persistence.support.JDOUserException;
import com.sun.jdo.spi.persistence.support.sqlstore.ActionDesc;

import java.util.ResourceBundle;

import org.glassfish.persistence.common.I18NHelper;

public class PersistentDeleted extends LifeCycleState {
    /**
     * I18N message handler
     */
    private final static ResourceBundle messages = I18NHelper.loadBundle(
            "com.sun.jdo.spi.persistence.support.sqlstore.Bundle", // NOI18N
            PersistentDeleted.class.getClassLoader());

    public PersistentDeleted() {
        // these flags are set only in the constructor
        // and shouldn't be changed afterwards
        // (cannot make them final since they are declared in superclass
        // but their values are specific to subclasses)
        isPersistent = true;
        isPersistentInDataStore = true;
        isTransactional = true;
        isDirty = true;
        isNew = false;
        isDeleted = true;
        isNavigable = false;
        isRefreshable = true;
        isBeforeImageUpdatable = false;
        needsRegister = true;
        needsReload = false;
        needsRestoreOnRollback = false;
        updateAction = ActionDesc.LOG_DESTROY;

        // The following flag does not allow merge
        needMerge = false;

        stateType = P_DELETED;
    }

    public LifeCycleState transitionCommit(boolean retainValues) {
        return changeState(TRANSIENT);
    }

    public LifeCycleState transitionFlushed() {
        return changeState(P_DELETED_FLUSHED);
    }

    public LifeCycleState transitionRefreshPersistent() {
        return changeState(P_CLEAN);
    }

    public LifeCycleState transitionRollback(boolean retainValues) {
        if (retainValues) {
            return changeState(P_NON_TX);
        } else {
            return changeState(HOLLOW);
        }
    }

    public LifeCycleState transitionReadField(boolean optimisitic, boolean nontransactionalRead,
                                              boolean transactionActive) {
        // Cannot read a deleted object
        throw new JDOUserException(I18NHelper.getMessage(messages,
                "jdo.lifecycle.deleted.accessField")); // NOI18N
    }

    public LifeCycleState transitionWriteField(boolean transactionActive) {
        // Cannot update a deleted object
        throw new JDOUserException(I18NHelper.getMessage(messages,
                "jdo.lifecycle.deleted.accessField")); // NOI18N
    }

    public LifeCycleState transitionRefresh() {
        return changeState(P_CLEAN);
    }
}









