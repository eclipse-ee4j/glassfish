/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation.
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

    /**
     * Indicates that a resource has been enlisted in a transaction.
     *
     * @see jakarta.transaction.Transaction#enlistResource(XAResource)
     */
    private boolean enlisted;

    /**
     * The busy state reflects the moment the resource is taken from the connection pool. It should be set to {@code true}
     * once it is taken from the pool, and it should be set to {@code false} once it is returned to the pool.
     * <p>
     * Setting to {@code true} happens at the moment the resource is taken from the pool (getResourceFromPool).<br>
     * Setting to {@code false} happens at the moment the connection is returned to the pool (resourceClosed).
     */
    private boolean busy;

    /**
     * Timestamp represents the time of resource creation, or the time the resource usage was complete and is handed back to
     * the pool. The timestamp value is used in the remove idle and invalid resources logic of the resource pool resizer.
     */
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

    /**
     * Resets all fields, representing the constructor call situation.
     */
    public void reset() {
        touchTimestamp();
        setBusy(false);
        setEnlisted(false);
    }

    @Override
    public String toString() {
        return "<ResourceState enlisted=" + enlisted + ", busy=" + busy + "/>";
    }
}
