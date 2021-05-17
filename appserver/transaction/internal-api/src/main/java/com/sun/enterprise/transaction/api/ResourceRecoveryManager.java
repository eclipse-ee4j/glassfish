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

package com.sun.enterprise.transaction.api;

import org.jvnet.hk2.annotations.Contract;

/**
 * ResourceRecoveryManager interface to be implemented by the resource manager
 * that supports XA recovery.
 *
 * @author Marina Vatkina
 */

@Contract
public interface ResourceRecoveryManager {

    /**
     * recover incomplete transactions
     * @param delegated indicates whether delegated recovery is needed
     * @param logPath transaction log directory path
     * @return boolean indicating the status of transaction recovery
     * @throws Exception when unable to recover
     */
    public boolean recoverIncompleteTx(boolean delegated, String logPath) throws Exception;

    /**
     * recover incomplete transactions with before and after event notifications
     * @param delegated indicates whether delegated recovery is needed
     * @param logPath transaction log directory path
     * @param instance the name opf the instance for which delegated recovery is requested, null if unknown
     * @param notifyRecoveryListeners specifies whether recovery listeners are to be notified
     * @return boolean indicating the status of transaction recovery
     * @throws Exception when unable to recover
     */
    public boolean recoverIncompleteTx(boolean delegated, String logPath, String instance,
            boolean notifyRecoveryListeners) throws Exception;

    /**
     * recover the xa-resources
     * @param force boolean to indicate if it has to be forced.
     */
    public void recoverXAResources(boolean force);

    /**
     * to recover xa resources
     */
    public void recoverXAResources();

    /**
     * to enable lazy recovery, setting lazy to "true" will
     *
     * @param lazy boolean
     */
    public void setLazyRecovery(boolean lazy);
}
