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
 *  AutoPersistentNewFlushedDeleted.java    March 10, 2000    Steffi Rauschenbach
 */

package com.sun.jdo.spi.persistence.support.sqlstore.state;

import com.sun.jdo.spi.persistence.support.sqlstore.ActionDesc;

public class AutoPersistentNewFlushedDeleted extends AutoPersistentNewDeleted {
    public AutoPersistentNewFlushedDeleted() {
        super();
        isPersistentInDataStore = true;
        updateAction = ActionDesc.LOG_DESTROY;
        stateType = AP_NEW_FLUSHED_DELETED;
    }

    public LifeCycleState transitionMakePersistent() {
        return changeState(P_NEW_FLUSHED_DELETED);
    }

    public LifeCycleState transitionFlushed() {
        return changeState(AP_NEW_DELETED);
    }
}








