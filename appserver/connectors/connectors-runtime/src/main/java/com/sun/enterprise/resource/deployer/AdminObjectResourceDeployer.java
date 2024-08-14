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

package com.sun.enterprise.resource.deployer;

import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.connectors.config.AdminObjectResource;
import org.glassfish.resourcebase.resources.api.ResourceDeployerInfo;
import org.glassfish.resourcebase.resources.api.ResourceInfo;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.types.Property;

/**
 * @author Srikanth P
 */
@Service
@Singleton
@ResourceDeployerInfo(AdminObjectResource.class)
public class AdminObjectResourceDeployer extends AbstractConnectorResourceDeployer<AdminObjectResource> {

    private static final Logger LOG = LogDomains.getLogger(AdminObjectResourceDeployer.class, LogDomains.RSR_LOGGER);

    @Inject
    private ConnectorRuntime runtime;


    @Override
    public synchronized void deployResource(AdminObjectResource resource, String applicationName, String moduleName) throws Exception {
        SimpleJndiName jndiName = SimpleJndiName.of(resource.getJndiName());
        ResourceInfo resourceInfo = new ResourceInfo(jndiName, applicationName, moduleName);
        createAdminObjectResource(resource, resourceInfo);
    }


    @Override
    public synchronized void deployResource(AdminObjectResource resource) throws Exception {
        ResourceInfo resourceInfo = ConnectorsUtil.getResourceInfo(resource);
        createAdminObjectResource(resource, resourceInfo);
    }

    private void createAdminObjectResource(AdminObjectResource aor, ResourceInfo resourceInfo)
            throws ConnectorRuntimeException {
        LOG.log(Level.FINE, "Calling backend to add adminObject: {0}", resourceInfo);
        runtime.addAdminObject(null, aor.getResAdapter(), resourceInfo,
                aor.getResType(), aor.getClassName(), transformProps(aor.getProperty()));
        LOG.log(Level.FINE, "Added adminObject in backend: {0}", resourceInfo);
    }


    @Override
    public void undeployResource(AdminObjectResource resource, String applicationName, String moduleName) throws Exception {
        SimpleJndiName jndiName = SimpleJndiName.of(resource.getJndiName());
        ResourceInfo resourceInfo = new ResourceInfo(jndiName, applicationName, moduleName);
        deleteAdminObjectResource(resource, resourceInfo);
    }


    @Override
    public synchronized void undeployResource(AdminObjectResource resource) throws Exception {
        final AdminObjectResource aor = resource;
        ResourceInfo resourceInfo = ConnectorsUtil.getResourceInfo(aor);
        deleteAdminObjectResource(aor, resourceInfo);
    }

    private void deleteAdminObjectResource(AdminObjectResource adminObject, ResourceInfo resourceInfo)
            throws ConnectorRuntimeException {
        LOG.log(Level.FINE, "Calling backend to delete adminObject: {0}", resourceInfo);
        runtime.deleteAdminObject(resourceInfo);
        LOG.log(Level.FINE, "Deleted adminObject in backend: {0}", resourceInfo);
    }


    @Override
    public boolean handles(Object resource) {
        return resource instanceof AdminObjectResource;
    }


    @Override
    public synchronized void disableResource(AdminObjectResource resource) throws Exception {
        undeployResource(resource);
    }


    @Override
    public synchronized void enableResource(AdminObjectResource resource) throws Exception {
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
