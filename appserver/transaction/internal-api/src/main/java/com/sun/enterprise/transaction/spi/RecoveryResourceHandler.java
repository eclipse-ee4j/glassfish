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

package com.sun.enterprise.transaction.spi;

import java.util.List;

import org.jvnet.hk2.annotations.Contract;

/**
 * RecoveryResourceHandler will be used by transaction-manager to get resource instances.<br>
 * Using these resource instances, transaction recovery will be done.<br>
 * All type of (xa)resources will have a resource recovery handler which will be used by
 * transaction-manager
 *
 * @author Jagadish Ramu
 */
@Contract
public interface RecoveryResourceHandler {
    /**
     * load xa-resource instances for recovery
     *
     * @param xaresList List of xa-resources, populate it with the xa-capable resources that needs recovery
     * @param connList  populate it with connections used to provide these xa-resources, if any.
     * Transaction-recovery will call close the connections in connList once recovery is over.
     */
    public void loadXAResourcesAndItsConnections(List xaresList, List connList);

    /**
     * close the connections that were used to provide xa-resources for recovery
     *
     * @param connList list of connections
     */
    public void closeConnections(List connList);
}
