/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation.
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

import javax.transaction.xa.XAResource;

public final class ResourceState {

    private boolean enlisted;
    private boolean busy;
    private long lastUsage;
    private long lastValidated;
    private int usageCount;

    ResourceState() {
        this.lastUsage = System.currentTimeMillis();
    }

    /**
     * Indicates that a resource has been enlisted in a transaction.
     *
     * @return true to indicate usage in transaction
     *
     * @see jakarta.transaction.Transaction#enlistResource(XAResource)
     */
    public boolean isEnlisted() {
        return enlisted;
    }

    /**
     * Indicates that a resource has been enlisted in a transaction.
     *
     * @param enlisted
     *
     * @see jakarta.transaction.Transaction#enlistResource(XAResource)
     */
    public void setEnlisted(boolean enlisted) {
        this.enlisted = enlisted;
    }


    /**
     * The busy state reflects the moment the resource is taken from the connection pool.
     * It should be set to {@code true} once it is taken from the pool, and it should be
     * set to {@code false} once it is returned to the pool.
     * <p>
     * Setting to {@code true} happens at the moment the resource is taken from the pool
     * (getResourceFromPool).<br>
     * Setting to {@code false} happens at the moment the connection is returned to the pool
     * (resourceClosed).
     *
     * @return true if used by app, false if in the pool
     */
    public boolean isBusy() {
        return busy;
    }

    /**
     * The busy state reflects the moment the resource is taken from the connection pool.
     * It should be set to {@code true} once it is taken from the pool, and it should be
     * set to {@code false} once it is returned to the pool.
     * <p>
     * Setting to {@code true} happens at the moment the resource is taken from the pool
     * (getResourceFromPool).<br>
     * Setting to {@code false} happens at the moment the connection is returned to the pool
     * (resourceClosed).
     *
     * @param busy true if used by app, false if in the pool
     */
    public void setBusy(boolean busy) {
        this.busy = busy;
    }


    /**
     * The timestamp value is used in the remove idle and invalid resources logic
     * of the resource pool resizer.
     *
     * @return last usage
     */
    public long getLastUsage() {
        return lastUsage;
    }

    /**
     * The timestamp value is used in the remove idle and invalid resources logic
     * of the resource pool resizer.
     *
     * @param lastUsage
     */
    public void setLastUsage(long lastUsage) {
        this.lastUsage = lastUsage;
    }

    /**
     * Holds the number of times the handle(connection) is used so far.
     * It is used by the ConnectionPool logic in case the "max-connection-usage-count" option is set
     * in the connection pool.
     *
     * @return number of usages
     */
    public int getUsageCount() {
        return this.usageCount;
    }

    /**
     * Holds the number of times the handle(connection) is used so far.
     * It is used by the ConnectionPool logic in case the "max-connection-usage-count" option is set
     * in the connection pool.
     */
    public void incrementUsageCount() {
        usageCount++;
    }


    /**
     * Holds the latest time at which the connection was validated.
     */
    public long getLastValidated() {
        return lastValidated;
    }

    /**
     * Holds the latest time at which the connection was validated.
     */
    public void setLastValidated(long lastValidated) {
        this.lastValidated = lastValidated;
    }

    /**
     * Resets all fields, representing the constructor call situation.
     */
    public void reset() {
        setBusy(false);
        setEnlisted(false);
    }

    @Override
    public String toString() {
        return "ResourceState[enlisted=" + enlisted + ", busy=" + busy + ", usages=" + usageCount + "]";
    }
}
