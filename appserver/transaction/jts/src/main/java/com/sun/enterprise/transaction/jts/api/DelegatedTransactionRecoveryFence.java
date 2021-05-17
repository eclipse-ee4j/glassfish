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

package com.sun.enterprise.transaction.jts.api;
/**
 *
 * @author mvatkina
 * Date: Sep 10, 2010
 */

/**
 * Interface to implement by delegated transaction recovery lock
 */

public interface DelegatedTransactionRecoveryFence {
    /**
     * Returns true if the specified instance on the specified path is being recovered
     * after the specified timestamp
     */
    public boolean isFenceRaised(String path, String instanceName, long timestamp);

    /**
     * Raise the fence for the specified instance on the specified path so that no other instance can
     * start delegated recovery at the same time.
     */
    public void raiseFence(String path, String instanceName);

    /**
     * Lower the fence for the specified instance on the specified path
     */
    public void lowerFence(String path, String instanceName);

    /**
     * Returns instance for which delegated recovery was done before the timestamp specified
     * on the specified path or null if such instance does not exist
     */
    public String getInstanceRecoveredFor(String path, long timestamp);

    /**
     * If an instance was doing delegated recovery on the specified path, assign
     * specified instance instead.
     */
    public void transferRecoveryTo(String path, String instanceName);

}
