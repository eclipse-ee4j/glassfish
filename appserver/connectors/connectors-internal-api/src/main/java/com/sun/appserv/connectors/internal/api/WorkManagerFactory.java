/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.appserv.connectors.internal.api;

import jakarta.resource.spi.work.WorkManager;

import org.jvnet.hk2.annotations.Contract;

@Contract
public interface WorkManagerFactory {
    /**;
     * get the proxy work manager object for the rar
     * @param threadPoolId thread pool id
     * @param moduleName resource-adapter-name
     * @return work-manager proxy
     * @throws ConnectorRuntimeException when unable to provide a proxy work manager
     */
    WorkManager getWorkManagerProxy(String threadPoolId, String moduleName, ClassLoader rarCL) throws ConnectorRuntimeException;

    /**
     * remove the work manager of the module (rar) from work-manager registry
     * @param moduleName resource-adapter-name
     * @return boolean indicating whether the work-manager is removed from registry or not
     */
    boolean removeWorkManager(String moduleName);

    /**
     * create a new work manager for the resource-adapter
     * @param threadPoolId thread-pool-id
     * @param raName resource-adapter-name
     * @return WorkManager
     */
    WorkManager createWorkManager(String threadPoolId, String raName, ClassLoader rarCL);
}
