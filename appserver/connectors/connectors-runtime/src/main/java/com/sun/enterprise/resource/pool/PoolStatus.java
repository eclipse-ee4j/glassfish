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

package com.sun.enterprise.resource.pool;

import org.glassfish.resourcebase.resources.api.PoolInfo;

/**
 * Reflects the current status of the Connection Pool. This class is used to get the status of the pool specifically the
 * number of connections free or used.
 *
 * @author Shalini M
 */
public class PoolStatus {

    private PoolInfo poolInfo;

    // Number of free connections in the pool
    private int numConnFree;

    // Number of connections in the pool that are being used currently.
    private int numConnUsed;

    public int getNumConnFree() {
        return numConnFree;
    }

    public void setNumConnFree(int numConnFree) {
        this.numConnFree = numConnFree;
    }

    public int getNumConnUsed() {
        return numConnUsed;
    }

    public void setNumConnUsed(int numConnUsed) {
        this.numConnUsed = numConnUsed;
    }

    public PoolInfo getPoolInfo() {
        return poolInfo;
    }

    public void setPoolInfo(PoolInfo poolInfo) {
        this.poolInfo = poolInfo;
    }

    public PoolStatus(PoolInfo poolInfo) {
        this.poolInfo = poolInfo;
    }

    @Override
    public String toString() {
        return "PoolStatus [poolInfo=" + poolInfo + ", numConnFree=" + numConnFree + ", numConnUsed=" + numConnUsed + "]";
    }
}
