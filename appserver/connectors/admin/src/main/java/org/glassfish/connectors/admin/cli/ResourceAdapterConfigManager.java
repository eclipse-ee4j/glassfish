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

import jakarta.resource.ResourceException;

import java.beans.PropertyVetoException;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.I18n;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.connectors.config.ResourceAdapterConfig;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.resourcebase.resources.api.ResourceStatus;
import org.glassfish.resources.admin.cli.ResourceManager;
import org.glassfish.resources.api.ResourceAttributes;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

import static org.glassfish.resources.admin.cli.ResourceConstants.RESOURCE_ADAPTER_CONFIG_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.THREAD_POOL_IDS;


/**
 * @author Jennifer Chou
 */
@Service (name=ServerTags.RESOURCE_ADAPTER_CONFIG)
@PerLookup
@I18n("create.resource.adapter.config")
public class ResourceAdapterConfigManager implements ResourceManager {

    private static final LocalStringManagerImpl I18N = new LocalStringManagerImpl(ResourceAdapterConfigManager.class);

    private String raName;
    private String threadPoolIds;
    private String objectType = "user";
    private String name;

    @Override
    public String getResourceType() {
        return ServerTags.RESOURCE_ADAPTER_CONFIG;
    }

    @Override
    public ResourceStatus create(Resources resources, ResourceAttributes attributes, final Properties properties,
        String target) throws Exception {
        setParams(attributes);

        ResourceStatus validationStatus = isValid(resources);
        if(validationStatus.getStatus() == ResourceStatus.FAILURE){
            return validationStatus;
        }

        try {
            ConfigSupport.apply(new SingleConfigCode<Resources>() {
                @Override
                public Object run(Resources param) throws PropertyVetoException, TransactionFailure {
                    ResourceAdapterConfig newResource = createConfigBean(param, properties);
                    param.getResources().add(newResource);
                    return newResource;
                }
            }, resources);

        } catch (TransactionFailure tfe) {
            Logger.getLogger(ResourceAdapterConfigManager.class.getName()).log(Level.SEVERE,
                    "TransactionFailure: create-resource-adapter-config", tfe);
            String msg = I18N.getLocalString("create.resource.adapter.config.fail",
                    "Unable to create resource adapter config", raName) +
                    " " + tfe.getLocalizedMessage();
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }

        String msg = I18N.getLocalString(
                "create.resource.adapter.config.success", "Resource adapter config {0} created successfully",
                raName);
        return new ResourceStatus(ResourceStatus.SUCCESS, msg);
    }

    private ResourceStatus isValid(Resources resources){
        ResourceStatus status = new ResourceStatus(ResourceStatus.SUCCESS, "Validation Successful");
        if (raName == null) {
            String msg = I18N.getLocalString("create.resource.adapter.confignoRAName",
                            "No RA Name defined for resource adapter config.");
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }
        // ensure we don't already have one of this name
        if (resources.getResourceByName(ResourceAdapterConfig.class, new SimpleJndiName(raName)) != null) {
            String msg = I18N.getLocalString("create.resource.adapter.config.duplicate",
                    "Resource adapter config already exists for RAR", raName);
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }
        return status;
    }

    private ResourceAdapterConfig createConfigBean(Resources param, Properties properties) throws PropertyVetoException,
            TransactionFailure {
        ResourceAdapterConfig newResource = param.createChild(ResourceAdapterConfig.class);
        newResource.setResourceAdapterName(raName);
        if(threadPoolIds != null) {
            newResource.setThreadPoolIds(threadPoolIds);
        }
        newResource.setObjectType(objectType);
        if (name != null) {
            newResource.setName(name);
        }
        if (properties != null) {
            for (Entry<?, ?> e : properties.entrySet()) {
                Property prop = newResource.createChild(Property.class);
                prop.setName((String) e.getKey());
                prop.setValue((String) e.getValue());
                newResource.getProperty().add(prop);
            }
        }
        return newResource;
    }

    public void setParams(ResourceAttributes attributes) {
        raName = attributes.getString(RESOURCE_ADAPTER_CONFIG_NAME);
        name = attributes.getString("name");
        threadPoolIds = attributes.getString(THREAD_POOL_IDS);
        objectType = attributes.getString(ServerTags.OBJECT_TYPE);
    }


    @Override
    public Resource createConfigBean(Resources resources, ResourceAttributes attributes, Properties properties,
        boolean validate) throws Exception {
        setParams(attributes);
        final ResourceStatus status;
        if (validate) {
            status = isValid(resources);
        } else {
            status = new ResourceStatus(ResourceStatus.SUCCESS, "");
        }
        if (status.getStatus() == ResourceStatus.SUCCESS) {
            return createConfigBean(resources, properties);
        }
        throw new ResourceException(status.getMessage(), status.getException());
    }
}
