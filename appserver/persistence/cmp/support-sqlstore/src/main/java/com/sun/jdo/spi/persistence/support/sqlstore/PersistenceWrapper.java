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
 * PersistenceWrapper.java
 *
 * Create on March 3, 2000
 *
 */

package com.sun.jdo.spi.persistence.support.sqlstore;


// This is a thin wrapper for Persistent class

public class PersistenceWrapper {
    public static final int IN_USE = 1;
    public static final int NOT_IN_USE = 2;
    public static final int DELETED = 3;

    // flags that indicate if all Persistent objects
    // in use for this primary key object, how many copies
    // of the instance exist (inUse > 0), and if any one of
    // them has been deleted (state == DELETED)
    private int state = 0;
    private int inUse = 0;

    // Persistent object itself
    private Object persistent = null;

    public void setState(int newstate) {
        this.state = newstate;
    }

    public int getState() {
        return state;
    }

    public void addInUse() {
        inUse++;
    }

    public int removeInUse() {
        if (inUse > 0) {
            inUse--;
        }
        return inUse;
    }

    public int getInUse() {
        return inUse;
    }

    public void setPersistent(Object newobject) {
        persistent = newobject;
    }

    public Object getPersistent() {
        return persistent;
    }

}
