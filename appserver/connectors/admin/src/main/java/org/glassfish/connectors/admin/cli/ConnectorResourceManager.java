/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2008, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.connectors.admin.cli;

import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;
import jakarta.resource.ResourceException;

import java.beans.PropertyVetoException;
import java.util.Properties;

import org.glassfish.api.I18n;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.connectors.config.ConnectorConnectionPool;
import org.glassfish.connectors.config.ConnectorResource;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.resourcebase.resources.admin.cli.ResourceUtil;
import org.glassfish.resourcebase.resources.api.ResourceStatus;
import org.glassfish.resourcebase.resources.util.BindableResourcesHelper;
import org.glassfish.resources.admin.cli.ResourceManager;
import org.glassfish.resources.api.ResourceAttributes;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

import static org.glassfish.resources.admin.cli.ResourceConstants.ENABLED;
import static org.glassfish.resources.admin.cli.ResourceConstants.JNDI_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.POOL_NAME;

/**
 * @author Jennifer Chou, Jagadish Ramu
 */
@Service(name = ServerTags.CONNECTOR_RESOURCE)
@PerLookup
@I18n("create.connector.resource")
public class ConnectorResourceManager implements ResourceManager {

    private static final String DESCRIPTION = ServerTags.DESCRIPTION;

    private static final LocalStringManagerImpl I18N = new LocalStringManagerImpl(ConnectorResourceManager.class);

    private String poolName;
    private String enabled = Boolean.TRUE.toString();
    private String enabledValueForTarget = Boolean.TRUE.toString();
    private String jndiName;
    private String description;
    private String objectType = "user";

    @Inject
    private ResourceUtil resourceUtil;

    @Inject
    private BindableResourcesHelper resourcesHelper;

    @Override
    public String getResourceType() {
        return ServerTags.CONNECTOR_RESOURCE;
    }

    @Override
    public ResourceStatus create(Resources resources, ResourceAttributes attributes, final Properties properties,
        String target) throws Exception {
        setAttributes(attributes, target);

        ResourceStatus validationStatus = isValid(resources, true, target);
        if(validationStatus.getStatus() == ResourceStatus.FAILURE){
            return validationStatus;
        }

        try {
            SingleConfigCode<Resources> configCode = param -> createResource(param, properties);
            ConfigSupport.apply(configCode, resources);
            if (!CommandTarget.TARGET_DOMAIN.equals(target)) {
                resourceUtil.createResourceRef(jndiName, enabledValueForTarget, target);
            }
        } catch (TransactionFailure tfe) {
            String msg = I18N.getLocalString("create.connector.resource.fail",
                    "Connector resource {0} create failed ", jndiName) +
                    " " + tfe.getLocalizedMessage();
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }

        String msg = I18N.getLocalString(
                "create.connector.resource.success", "Connector resource {0} created successfully",
                jndiName);
        return new ResourceStatus(ResourceStatus.SUCCESS, msg);

    }

    private ResourceStatus isValid(Resources resources, boolean validateResourceRef, String target){
        ResourceStatus status ;
        if (jndiName == null) {
            String msg = I18N.getLocalString("create.connector.resource.noJndiName",
                    "No JNDI name defined for connector resource.");
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }

        status = resourcesHelper.validateBindableResourceForDuplicates(resources, jndiName, validateResourceRef,
                target, ConnectorResource.class);
        if(status.getStatus() == ResourceStatus.FAILURE){
            return status;
        }

        if (!isConnPoolExists(resources)) {
            String msg = I18N.getLocalString("create.connector.resource.connPoolNotFound",
                    "Attribute value (pool-name = {0}) is not found in list of connector connection pools.", poolName);
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }
        return status;
    }


    private ConnectorResource createResource(Resources param, Properties props) throws PropertyVetoException,
            TransactionFailure {
        ConnectorResource newResource = createConfigBean(param, props);
        param.getResources().add(newResource);
        return newResource;
    }


    private ConnectorResource createConfigBean(Resources param, Properties properties) throws PropertyVetoException,
            TransactionFailure {
        ConnectorResource newResource = param.createChild(ConnectorResource.class);
        newResource.setJndiName(jndiName);
        if (description != null) {
            newResource.setDescription(description);
        }
        newResource.setPoolName(poolName);
        newResource.setEnabled(enabled);
        newResource.setObjectType(objectType);
        if (properties != null) {
            for (String propertyName : properties.stringPropertyNames()) {
                Property prop = newResource.createChild(Property.class);
                prop.setName(propertyName);
                prop.setValue(properties.getProperty(propertyName));
                newResource.getProperty().add(prop);
            }
        }
        return newResource;
    }

    private void setAttributes(ResourceAttributes attributes, String target) {
        poolName = attributes.getString(POOL_NAME);
        if (target == null) {
            enabled = attributes.getString(ENABLED);
        } else {
            enabled = resourceUtil.computeEnabledValueForResourceBasedOnTarget(attributes.getString(ENABLED), target);
        }
        enabledValueForTarget = attributes.getString(ENABLED);
        jndiName = attributes.getString(JNDI_NAME);
        description = attributes.getString(DESCRIPTION);
        objectType = attributes.getString(ServerTags.OBJECT_TYPE);
    }

    private boolean isConnPoolExists(Resources resources) {
        final SimpleJndiName jndiPoolName = new SimpleJndiName(poolName);
        return resources.getResourceByName(ConnectorConnectionPool.class, jndiPoolName) != null;
    }


    @Override
    public Resource createConfigBean(Resources resources, ResourceAttributes attributes, Properties properties,
        boolean validate) throws Exception {
        setAttributes(attributes, null);
        final ResourceStatus status;
        if (validate) {
            status = isValid(resources, false, null);
        } else {
            status = new ResourceStatus(ResourceStatus.SUCCESS, "");
        }
        if (status.getStatus() == ResourceStatus.SUCCESS) {
            return createConfigBean(resources, properties);
        }
        throw new ResourceException(status.getMessage(), status.getException());
    }
}
