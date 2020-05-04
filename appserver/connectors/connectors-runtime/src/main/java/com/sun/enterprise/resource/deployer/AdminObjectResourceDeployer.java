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

package com.sun.enterprise.resource.deployer;

import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.logging.LogDomains;
import org.glassfish.connectors.config.AdminObjectResource;
import org.glassfish.resourcebase.resources.api.ResourceDeployer;
import org.glassfish.resourcebase.resources.api.ResourceDeployerInfo;
import org.glassfish.resourcebase.resources.api.ResourceInfo;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.types.Property;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Srikanth P
 */

@Service
@ResourceDeployerInfo(AdminObjectResource.class)
@Singleton
public class AdminObjectResourceDeployer extends AbstractConnectorResourceDeployer {

    @Inject
    private ConnectorRuntime runtime;

    private static Logger _logger = LogDomains.getLogger(AdminObjectResourceDeployer.class, LogDomains.RSR_LOGGER);

    /**
     * {@inheritDoc}
     */
    public synchronized void deployResource(Object resource, String applicationName, String moduleName) throws Exception {
        final AdminObjectResource aor = (AdminObjectResource) resource;
        String jndiName = aor.getJndiName();
        ResourceInfo resourceInfo = new ResourceInfo(jndiName, applicationName, moduleName);
        createAdminObjectResource(aor, resourceInfo);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void deployResource(Object resource) throws Exception {

        final AdminObjectResource aor = (AdminObjectResource) resource;
        ResourceInfo resourceInfo = ConnectorsUtil.getResourceInfo(aor);
        createAdminObjectResource(aor, resourceInfo);
    }

    private void createAdminObjectResource(AdminObjectResource aor, ResourceInfo resourceInfo)
            throws ConnectorRuntimeException {
        /* TODO Not needed any more ?

                        if (aor.isEnabled()) {
                            //registers the jsr77 object for the admin object resource deployed
                            final ManagementObjectManager mgr =
                                getAppServerSwitchObject().getManagementObjectManager();
                            mgr.registerAdminObjectResource(jndiName,
                                aor.getResAdapter(), aor.getResType(),
                                getPropNamesAsStrArr(aor.getElementProperty()),
                                getPropValuesAsStrArr(aor.getElementProperty()));
                        } else {
                                _logger.log(Level.INFO, "core.resource_disabled",
                                        new Object[] {jndiName,
                                        IASJ2EEResourceFactoryImpl.JMS_RES_TYPE});
                        }
                */

        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "Calling backend to add adminObject", resourceInfo);
        }
        runtime.addAdminObject(null, aor.getResAdapter(), resourceInfo,
                aor.getResType(), aor.getClassName(), transformProps(aor.getProperty()));
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "Added adminObject in backend", resourceInfo);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void undeployResource(Object resource, String applicationName, String moduleName) throws Exception {
        final AdminObjectResource aor = (AdminObjectResource) resource;
        ResourceInfo resourceInfo = new ResourceInfo(aor.getJndiName(), applicationName, moduleName);
        deleteAdminObjectResource(aor, resourceInfo);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void undeployResource(Object resource)
            throws Exception {
        final AdminObjectResource aor = (AdminObjectResource) resource;
        ResourceInfo resourceInfo = ConnectorsUtil.getResourceInfo(aor);
        deleteAdminObjectResource(aor, resourceInfo);
    }

    private void deleteAdminObjectResource(AdminObjectResource adminObject, ResourceInfo resourceInfo)
            throws ConnectorRuntimeException {

        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "Calling backend to delete adminObject", resourceInfo);
        }
        runtime.deleteAdminObject(resourceInfo);
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "Deleted adminObject in backend", resourceInfo);
        }

        //unregister the managed object
        /* TODO Not needed any more ?
            final ManagementObjectManager mgr =
                    getAppServerSwitchObject().getManagementObjectManager();
            mgr.unregisterAdminObjectResource(aor.getJndiName(), aor.getResType());
        */
    }

    /**
     * {@inheritDoc}
     */
    public boolean handles(Object resource) {
        return resource instanceof AdminObjectResource;
    }

    /**
     * @inheritDoc
     */
    public boolean supportsDynamicReconfiguration() {
        return false;
    }

    /**
     * @inheritDoc
     */
    public Class[] getProxyClassesForDynamicReconfiguration() {
        return new Class[0];
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void redeployResource(Object resource)
            throws Exception {
        undeployResource(resource);
        deployResource(resource);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void disableResource(Object resource)
            throws Exception {
        undeployResource(resource);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void enableResource(Object resource)
            throws Exception {
        deployResource(resource);
    }

    private Properties transformProps(List<Property> domainProps) {

        Properties props = new Properties();
        for (Property domainProp : domainProps) {
            props.setProperty(domainProp.getName(), domainProp.getValue());
        }
        return props;
    }
}
