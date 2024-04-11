/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

/**
 * Abstract Pool manager for unimplemented features. Throws UnsupportedOperationException when invoked.
 */
public abstract class AbstractPoolManager implements PoolManager {

// Not used in the code
//    @Override
//    public void emptyResourcePool(ResourceSpec spec) {
//        throw new UnsupportedOperationException();
//    }

// Not used in the code
//    @Override
//    public void killAllPools() {
//        throw new UnsupportedOperationException();
//    }

// Not used in the code
//    @Override
//    public void setSelfManaged(PoolInfo poolInfo, boolean flag) {
//        throw new UnsupportedOperationException();
//    }

// Not used in the code
//    public ConcurrentHashMap getMonitoredPoolTable() {
//        throw new UnsupportedOperationException();
//    }
}
