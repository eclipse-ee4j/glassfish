/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

package org.glassfish.resources.admin.cli;

import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;
import jakarta.resource.ResourceException;

import java.beans.PropertyVetoException;
import java.util.Properties;

import org.glassfish.api.I18n;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.resourcebase.resources.admin.cli.ResourceUtil;
import org.glassfish.resourcebase.resources.api.ResourceStatus;
import org.glassfish.resourcebase.resources.util.BindableResourcesHelper;
import org.glassfish.resources.api.ResourceAttributes;
import org.glassfish.resources.config.CustomResource;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

import static com.sun.enterprise.config.serverbeans.ServerTags.DESCRIPTION;
import static org.glassfish.resources.admin.cli.ResourceConstants.ENABLED;
import static org.glassfish.resources.admin.cli.ResourceConstants.FACTORY_CLASS;
import static org.glassfish.resources.admin.cli.ResourceConstants.JNDI_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.RES_TYPE;


@Service(name= ServerTags.CUSTOM_RESOURCE)
@PerLookup
@I18n("create.custom.resource")
public class CustomResourceManager implements ResourceManager {

    private static final LocalStringManagerImpl I18N = new LocalStringManagerImpl(CustomResourceManager.class);

    @Inject
    private ResourceUtil resourceUtil;

    @Inject
    private BindableResourcesHelper resourcesHelper;

    private String resType;
    private String factoryClass;
    private String enabled;
    private String enabledValueForTarget;
    private String description;
    private String jndiName;

    @Override
    public String getResourceType() {
        return ServerTags.CUSTOM_RESOURCE;
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
            String msg = I18N.getLocalString(
                    "create.custom.resource.fail",
                    "Unable to create custom resource {0}.", jndiName) +
                    " " + tfe.getLocalizedMessage();
            return new ResourceStatus(ResourceStatus.FAILURE, msg, true);
        }

        String msg = I18N.getLocalString(
                "create.custom.resource.success",
                "Custom Resource {0} created.", jndiName);
        return new ResourceStatus(ResourceStatus.SUCCESS, msg, true);
    }

    private ResourceStatus isValid(Resources resources, boolean validateResourceRef, String target){
        ResourceStatus status ;
        if (resType == null) {
            String msg = I18N.getLocalString(
                    "create.custom.resource.noResType",
                    "No type defined for Custom Resource.");
            return new ResourceStatus(ResourceStatus.FAILURE, msg, true);
        }

        if (factoryClass == null) {
            String msg = I18N.getLocalString(
                    "create.custom.resource.noFactoryClassName",
                    "No Factory class name defined for Custom Resource.");
            return new ResourceStatus(ResourceStatus.FAILURE, msg, true);
        }


        status = resourcesHelper.validateBindableResourceForDuplicates(resources, jndiName, validateResourceRef,
                target, CustomResource.class);
        if(status.getStatus() == ResourceStatus.FAILURE){
            return status;
        }

        return status;
    }

    private void setAttributes(ResourceAttributes attributes, String target) {
        jndiName = attributes.getString(JNDI_NAME);
        resType =  attributes.getString(RES_TYPE);
        factoryClass = attributes.getString(FACTORY_CLASS);
        if (target == null) {
            enabled = attributes.getString(ENABLED);
        } else {
            enabled = resourceUtil.computeEnabledValueForResourceBasedOnTarget(attributes.getString(ENABLED), target);
        }
        enabledValueForTarget = attributes.getString(ENABLED);
        description = attributes.getString(DESCRIPTION);
    }

    private Object createResource(Resources param, Properties properties) throws PropertyVetoException,
            TransactionFailure {
        CustomResource newResource = createConfigBean(param, properties);
        param.getResources().add(newResource);
        return newResource;
    }

    private CustomResource createConfigBean(Resources param, Properties properties) throws PropertyVetoException,
            TransactionFailure {
        CustomResource newResource = param.createChild(CustomResource.class);
        newResource.setJndiName(jndiName);
        newResource.setFactoryClass(factoryClass);
        newResource.setResType(resType);
        newResource.setEnabled(enabled);
        if (description != null) {
            newResource.setDescription(description);
        }
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
