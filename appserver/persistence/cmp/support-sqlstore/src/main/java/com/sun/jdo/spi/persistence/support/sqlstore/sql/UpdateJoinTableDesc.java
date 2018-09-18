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
 * UpdateJoinTableDesc.java
 *
 * Created on March 3, 2000
 *
 */

package com.sun.jdo.spi.persistence.support.sqlstore.sql;

import com.sun.jdo.spi.persistence.support.sqlstore.SQLStateManager;

/**
 *
 */
public class UpdateJoinTableDesc extends Object {

    private SQLStateManager parentSM;

    private SQLStateManager foreignSM;

    private int action;

    public UpdateJoinTableDesc(SQLStateManager parentSM, SQLStateManager foreignSM, int action) {
        super();

        this.parentSM = parentSM;
        this.foreignSM = foreignSM;
        this.action = action;
    }

    public SQLStateManager getForeignStateManager() {
        return foreignSM;
    }

    public SQLStateManager getParentStateManager() {
        return parentSM;
    }

    public int getAction() {
        return action;
    }

}

