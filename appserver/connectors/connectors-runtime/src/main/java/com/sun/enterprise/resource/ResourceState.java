/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.resource;

public class ResourceState {
    private boolean enlisted;
    private boolean busy;
    private long timestamp;

    public boolean isEnlisted() {
        return enlisted;
    }

    public boolean isUnenlisted() {
        return !enlisted;
    }

    public boolean isFree() {
        return !busy;
    }

    public void setEnlisted(boolean enlisted) {
        this.enlisted = enlisted;
    }

    public boolean isBusy() {
        return busy;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void touchTimestamp() {
        timestamp = System.currentTimeMillis();
    }

    public ResourceState() {
        touchTimestamp();
    }

    @Override
    public String toString() {
        return "Enlisted :" + enlisted + " Busy :" + busy;
    }
}
