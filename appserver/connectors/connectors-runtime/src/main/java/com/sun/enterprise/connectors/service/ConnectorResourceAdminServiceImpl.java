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

package com.sun.enterprise.connectors.service;

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.appserv.connectors.internal.spi.ConnectorNamingEvent;
import com.sun.enterprise.connectors.ConnectorConnectionPool;
import com.sun.enterprise.connectors.ConnectorDescriptorInfo;
import com.sun.enterprise.connectors.naming.ConnectorNamingEventNotifier;
import com.sun.enterprise.connectors.naming.ConnectorResourceNamingEventNotifier;

import java.util.Hashtable;
import java.util.logging.Level;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.resourcebase.resources.api.PoolInfo;
import org.glassfish.resourcebase.resources.api.ResourceInfo;
import org.glassfish.resourcebase.resources.naming.ResourceNamingService;
import org.glassfish.resources.naming.SerializableObjectRefAddr;

/**
 * This is connector resource admin service. It creates and deletes the
 * connector resources.
 *
 * @author Srikanth P
 */
public class ConnectorResourceAdminServiceImpl extends ConnectorService {

    private final ResourceNamingService namingService = _runtime.getResourceNamingService();

    /**
     * Creates the connector resource on a given connection pool
     *
     * @param resourceInfo     JNDI name of the resource to be created
     * @param poolInfo     PoolName to which the connector resource belongs.
     * @param resourceType Resource type Unused.
     * @throws ConnectorRuntimeException If the resouce creation fails.
     */
    public void createConnectorResource(ResourceInfo resourceInfo, PoolInfo poolInfo, String resourceType)
        throws ConnectorRuntimeException {
        ConnectorConnectionPool ccp = null;
        SimpleJndiName jndiNameForPool = ConnectorAdminServiceUtils.getReservePrefixedJNDINameForPool(poolInfo);
        try {
            ccp = (ConnectorConnectionPool) namingService.lookup(poolInfo, jndiNameForPool);
        } catch (NamingException ne) {
            _logger.log(Level.WARNING,
                "Probably the pool {0} is not yet initialized (lazy-loading), trying to check ...", poolInfo);
            try {
                checkAndLoadPool(poolInfo);
                ccp = (ConnectorConnectionPool) namingService.lookup(poolInfo, jndiNameForPool);
            } catch (NamingException e) {
                _logger.log(Level.SEVERE, "Second lookup failed for {0}: {1}", new Object[] {poolInfo, e});
                throw new ConnectorRuntimeException(
                    "Second lookup failed again for pool name " + jndiNameForPool + " or " + poolInfo, ne);
            }
        }

        ConnectorDescriptorInfo cdi = ccp.getConnectorDescriptorInfo();
        Reference ref = new Reference(cdi.getConnectionFactoryClass(),
            "com.sun.enterprise.resource.naming.ConnectorObjectFactory", null);
        RefAddr addr = new SerializableObjectRefAddr(PoolInfo.class.getName(), poolInfo);
        ref.add(addr);
        addr = new StringRefAddr("rarName", cdi.getRarName());
        ref.add(addr);
        RefAddr resAddr = new SerializableObjectRefAddr(ResourceInfo.class.getName(), resourceInfo);
        ref.add(resAddr);

        try {
            namingService.publishObject(resourceInfo, ref, true);
            _registry.addResourceInfo(resourceInfo);
        } catch (NamingException ne) {
            throw new ConnectorRuntimeException("Failed to publish connection factory for " + poolInfo, ne);
        }

        // To notify that a connector resource rebind has happened.
        ConnectorResourceNamingEventNotifier.getInstance().notifyListeners(
            new ConnectorNamingEvent(resourceInfo.getName(), ConnectorNamingEvent.EVENT_OBJECT_REBIND));
    }

    /**
     * Deletes the connector resource.
     *
     * @param resourceInfo JNDI name of the resource to delete.
     * @throws ConnectorRuntimeException if connector resource deletion fails.
     */
    public void deleteConnectorResource(ResourceInfo resourceInfo) throws ConnectorRuntimeException {
        try {
            namingService.unpublishObject(resourceInfo);
        } catch (NamingException ne) {
            if (ne instanceof NameNotFoundException) {
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.log(Level.FINE, "rardeployment.connectorresource_removal_from_jndi_error", resourceInfo);
                    _logger.log(Level.FINE, "", ne);
                }
                return;
            }
            throw new ConnectorRuntimeException("Failed to delete connector resource from jndi", ne);
        } finally {
            _registry.removeResourceInfo(resourceInfo);
        }
    }

    /**
     * Gets Connector Resource Rebind Event notifier.
     *
     * @return ConnectorNamingEventNotifier
     */
    public ConnectorNamingEventNotifier getResourceRebindEventNotifier() {
        return ConnectorResourceNamingEventNotifier.getInstance();
    }


    /**
     * Look up the JNDI name with appropriate suffix.
     * Suffix can be either __pm or __nontx.
     *
     * @param resourceInfo resource-name
     * @return Object - from jndi
     * @throws NamingException - when unable to get the object form jndi
     */
    public <T> T lookup(ResourceInfo resourceInfo) throws NamingException {
        _logger.log(Level.FINEST, "lookup(resourceInfo={0})", resourceInfo);
        // To pass suffix that will be used by connector runtime during lookup
        final String suffix = ConnectorsUtil.getValidSuffix(resourceInfo.getName());
        final SimpleJndiName jndiName;
        final Hashtable<Object, Object> env;
        if (suffix == null) {
            env = null;
            jndiName = resourceInfo.getName();
        } else {
            env = new Hashtable<>();
            env.put(ConnectorConstants.JNDI_SUFFIX_PROPERTY, suffix);
            jndiName = resourceInfo.getName().removeSuffix(suffix);
        }
        ResourceInfo actualResourceInfo = new ResourceInfo(jndiName, resourceInfo.getApplicationName(),
                resourceInfo.getModuleName());
        return namingService.lookup(actualResourceInfo, actualResourceInfo.getName(), env);
    }
}
