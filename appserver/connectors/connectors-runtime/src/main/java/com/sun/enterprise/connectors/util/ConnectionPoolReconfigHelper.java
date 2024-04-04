/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.connectors.util;

import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.enterprise.connectors.ConnectorConnectionPool;
import com.sun.enterprise.connectors.ConnectorDescriptorInfo;
import com.sun.enterprise.connectors.authentication.ConnectorSecurityMap;
import com.sun.enterprise.connectors.authentication.RuntimeSecurityMap;
import com.sun.logging.LogDomains;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ConnectionPoolReconfigHelper {

    private static final Logger _logger = LogDomains.getLogger(ConnectionPoolReconfigHelper.class,
        LogDomains.RSR_LOGGER);

    public enum ReconfigAction {
        /**
         * Recreate connection pool
         */
        RECREATE_POOL,
        /**
         * Update ManagedConnectionFactory and attributes
         */
        UPDATE_MCF_AND_ATTRIBUTES,
        /**
         * No operation
         */
        NO_OP
    }

    public static ReconfigAction compare(ConnectorConnectionPool oldPool, ConnectorConnectionPool newPool,
        Set excludedProps) throws ConnectorRuntimeException {
        if (isEqualConnectorConnectionPool(oldPool, newPool, excludedProps) == ReconfigAction.NO_OP) {
            return ReconfigAction.UPDATE_MCF_AND_ATTRIBUTES;
        }
        return ReconfigAction.RECREATE_POOL;
    }


    /**
     * Compare the Original ConnectorConnectionPool with the passed one
     * If MCF properties are changed, indicate that pool recreation is
     * required
     * We only check the MCF properties since a pool restart is required
     * for changes in MCF props. For pool specific properties we can get
     * away without restart
     * If the new pool and old pool have identical MCF properties returns
     * true
     */
    private static ReconfigAction isEqualConnectorConnectionPool(ConnectorConnectionPool oldCcp,
        ConnectorConnectionPool newCcp, Set excludedProps) {

        //for all the following properties, we need to recreate pool if they
        //have changed
        if ((newCcp.isPoolingOn() != oldCcp.isPoolingOn()) || (newCcp.getTransactionSupport() != oldCcp.getTransactionSupport()) || (newCcp.isAssociateWithThread() != oldCcp.isAssociateWithThread()) || (newCcp.isLazyConnectionAssoc() != oldCcp.isLazyConnectionAssoc())) {
            return ReconfigAction.RECREATE_POOL;
        }

        if (newCcp.isPartitionedPool() != oldCcp.isPartitionedPool()) {
            return ReconfigAction.RECREATE_POOL;
        }
        if (newCcp.getPoolDataStructureType() == null && oldCcp.getPoolDataStructureType() != null) {
            return ReconfigAction.RECREATE_POOL;
        }
        if (newCcp.getPoolDataStructureType() != null && oldCcp.getPoolDataStructureType() == null) {
            return ReconfigAction.RECREATE_POOL;
        }

        if (((newCcp.getPoolDataStructureType() != null) && (oldCcp.getPoolDataStructureType() != null)
                && !(newCcp.getPoolDataStructureType().equals(oldCcp.getPoolDataStructureType())))) {
            return ReconfigAction.RECREATE_POOL;
        }

        if ((newCcp.getPoolWaitQueue() != null) && (oldCcp.getPoolWaitQueue() == null)) {
            return ReconfigAction.RECREATE_POOL;
        }

        if ((newCcp.getPoolWaitQueue() == null) && (oldCcp.getPoolWaitQueue() != null)) {
            return ReconfigAction.RECREATE_POOL;
        }

        if ((newCcp.getPoolWaitQueue() != null) && (oldCcp.getPoolWaitQueue() != null)
                && (!newCcp.getPoolWaitQueue().equals(oldCcp.getPoolWaitQueue()))) {
            return ReconfigAction.RECREATE_POOL;
        }

        if ((newCcp.getDataStructureParameters() != null) && (oldCcp.getDataStructureParameters() == null)) {
            return ReconfigAction.RECREATE_POOL;
        }

        if ((newCcp.getDataStructureParameters() == null) && (oldCcp.getDataStructureParameters() != null)) {
            return ReconfigAction.RECREATE_POOL;
        }


        if ((newCcp.getDataStructureParameters() != null) && (oldCcp.getDataStructureParameters() != null)
                && !(newCcp.getDataStructureParameters().equals(oldCcp.getDataStructureParameters()))) {
            return ReconfigAction.RECREATE_POOL;
        }

        ConnectorDescriptorInfo oldCdi = oldCcp.getConnectorDescriptorInfo();
        ConnectorDescriptorInfo newCdi = newCcp.getConnectorDescriptorInfo();

        if (!oldCdi.getResourceAdapterClassName().equals(
                newCdi.getResourceAdapterClassName())) {

            logFine(
                    "isEqualConnectorConnectionPool: getResourceAdapterClassName:: " +
                            oldCdi.getResourceAdapterClassName() + " -- " +
                            newCdi.getResourceAdapterClassName());
            return ReconfigAction.RECREATE_POOL;
        }

        if (!oldCdi.getConnectionDefinitionName().equals(
                newCdi.getConnectionDefinitionName())) {

            logFine(
                    "isEqualConnectorConnectionPool: getConnectionDefinitionName:: " +
                            oldCdi.getConnectionDefinitionName() + " -- " +
                            newCdi.getConnectionDefinitionName());

            return ReconfigAction.RECREATE_POOL;
        }

        ConnectorSecurityMap[] newSecurityMaps = newCcp.getSecurityMaps();
        RuntimeSecurityMap newRuntimeSecurityMap = SecurityMapUtils.processSecurityMaps(newSecurityMaps);
        ConnectorSecurityMap[] oldSecurityMaps = oldCcp.getSecurityMaps();
        RuntimeSecurityMap oldRuntimeSecurityMap = SecurityMapUtils.processSecurityMaps(oldSecurityMaps);
        if (!(oldRuntimeSecurityMap.equals(newRuntimeSecurityMap))) {
            logFine("isEqualConnectorConnectionPool: CCP.getSecurityMaps:: " +
                    "New set of Security Maps is not equal to the existing" +
                    " set of security Maps.");
            return ReconfigAction.RECREATE_POOL;
        }
        return oldCdi.compareMCFConfigProperties(newCdi, excludedProps);
    }

    private static void logFine(String msg) {
        if (_logger.isLoggable(Level.FINE) && msg != null) {
            _logger.fine(msg);
        }
    }
}
