/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.concurrent.admin;

import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;
import jakarta.resource.ResourceException;

import java.beans.PropertyVetoException;
import java.util.Properties;

import org.glassfish.api.I18n;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.concurrent.config.ManagedThreadFactory;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.resourcebase.resources.admin.cli.ResourceUtil;
import org.glassfish.resourcebase.resources.api.ResourceStatus;
import org.glassfish.resourcebase.resources.util.BindableResourcesHelper;
import org.glassfish.resources.admin.cli.ResourceManager;
import org.glassfish.resources.api.ResourceAttributes;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.ConfiguredBy;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.CONTEXT_INFO;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.CONTEXT_INFO_DEFAULT_VALUE;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.CONTEXT_INFO_ENABLED;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.THREAD_PRIORITY;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.USE_VIRTUAL_THREADS;
import static org.glassfish.resources.admin.cli.ResourceConstants.ENABLED;
import static org.glassfish.resources.admin.cli.ResourceConstants.JNDI_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.SYSTEM_ALL_REQ;

/**
 *
 * The managed thread factory manager allows you to create and delete
 * the managed-thread-factory config element
 */
@Service (name=ServerTags.MANAGED_THREAD_FACTORY)
@I18n("managed.thread.factory.manager")
@ConfiguredBy(Resources.class)
public class ManagedThreadFactoryManager implements ResourceManager {

    private static final LocalStringManagerImpl I18N = new LocalStringManagerImpl(ManagedThreadFactoryManager.class);
    private static final String DESCRIPTION = ServerTags.DESCRIPTION;

    private String jndiName = null;
    private String description = null;
    private String threadPriority = ""+Thread.NORM_PRIORITY;
    private String virtual = Boolean.FALSE.toString();
    private String contextInfoEnabled = Boolean.TRUE.toString();
    private String contextInfo = CONTEXT_INFO_DEFAULT_VALUE;
    private String enabled = Boolean.TRUE.toString();
    private String enabledValueForTarget = Boolean.TRUE.toString();

    @Inject
    private ResourceUtil resourceUtil;

    @Inject
    private ServerEnvironment environment;

    @Inject
    private BindableResourcesHelper resourcesHelper;

    @Override
    public String getResourceType () {
        return ServerTags.MANAGED_THREAD_FACTORY;
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
            String msg = I18N.getLocalString("create.managed.thread.factory.failed", "Managed thread factory {0} creation failed", jndiName) + " " + tfe.getLocalizedMessage();
            ResourceStatus status = new ResourceStatus(ResourceStatus.FAILURE, msg);
            status.setException(tfe);
            return status;
        }
        String msg = I18N.getLocalString("create.managed.thread.factory.success", "Managed thread factory {0} created successfully", jndiName);
        return new ResourceStatus(ResourceStatus.SUCCESS, msg);
    }

    private ResourceStatus isValid(Resources resources, boolean validateResourceRef, String target){
        ResourceStatus status ;


        if (jndiName == null) {
            String msg = I18N.getLocalString("managed.thread.factory.noJndiName", "No JNDI name defined for managed thread factory.");
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }

        status = resourcesHelper.validateBindableResourceForDuplicates(resources, jndiName, validateResourceRef, target, ManagedThreadFactory.class);

        return status;
    }

    private void setAttributes(ResourceAttributes attributes, String target) {
        jndiName = attributes.getString(JNDI_NAME);
        description = attributes.getString(DESCRIPTION);
        contextInfoEnabled = attributes.getString(CONTEXT_INFO_ENABLED);
        contextInfo = attributes.getString(CONTEXT_INFO);
        threadPriority = attributes.getString(THREAD_PRIORITY);
        virtual = attributes.getString(USE_VIRTUAL_THREADS);
        if (target == null) {
            enabled = attributes.getString(ENABLED);
        } else {
            enabled = resourceUtil.computeEnabledValueForResourceBasedOnTarget(attributes.getString(ENABLED), target);
        }
        enabledValueForTarget = attributes.getString(ENABLED);
    }

    private ManagedThreadFactory createResource(Resources param, Properties properties) throws PropertyVetoException,
            TransactionFailure {
        ManagedThreadFactory newResource = createConfigBean(param, properties);
        param.getResources().add(newResource);
        return newResource;
    }

    private ManagedThreadFactory createConfigBean(Resources param, Properties properties) throws PropertyVetoException,
            TransactionFailure {
        ManagedThreadFactory managedThreadFactory = param.createChild(ManagedThreadFactory.class);
        managedThreadFactory.setJndiName(jndiName);
        if (description != null) {
            managedThreadFactory.setDescription(description);
        }
        managedThreadFactory.setContextInfoEnabled(contextInfoEnabled);
        managedThreadFactory.setContextInfo(contextInfo);
        managedThreadFactory.setThreadPriority(threadPriority);
        managedThreadFactory.setUseVirtualThreads(virtual);
        managedThreadFactory.setEnabled(enabled);
        if (properties != null) {
            for (String propertyName : properties.stringPropertyNames()) {
                Property prop = managedThreadFactory.createChild(Property.class);
                prop.setName(propertyName);
                prop.setValue(properties.getProperty(propertyName));
                managedThreadFactory.getProperty().add(prop);
            }
        }
        return managedThreadFactory;
    }

    @Override
    public Resource createConfigBean(final Resources resources, ResourceAttributes attributes,
        final Properties properties, boolean validate) throws Exception {
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


    public ResourceStatus delete(final Resources resources, final SimpleJndiName jndiName, final String target)
        throws Exception {
        if (jndiName == null) {
            String msg = I18N.getLocalString("managed.thread.factory.noJndiName", "No JNDI name defined for managed thread factory.");
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }
        Resource resource = resources.getResourceByName(ManagedThreadFactory.class, jndiName);

        // ensure we already have this resource
        if (resource == null){
            String msg = I18N.getLocalString("delete.managed.thread.factory.notfound", "A managed thread factory named {0} does not exist.", jndiName);
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }

        if (SYSTEM_ALL_REQ.equals(resource.getObjectType())) {
            String msg = I18N.getLocalString("delete.concurrent.resource.notAllowed", "The {0} resource cannot be deleted as it is required to be configured in the system.", jndiName);
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }

        if (environment.isDas()) {

            if (CommandTarget.TARGET_DOMAIN.equals(target)) {
                if (!resourceUtil.getTargetsReferringResourceRef(jndiName).isEmpty()) {
                    String msg = I18N.getLocalString("delete.managed.thread.factory.resource-ref.exist", "This managed thread factory [ {0} ] is referenced in an instance/cluster target, use delete-resource-ref on appropriate target", jndiName);
                    return new ResourceStatus(ResourceStatus.FAILURE, msg);
                }
            } else {
                if (!resourceUtil.isResourceRefInTarget(jndiName, target)) {
                    String msg = I18N.getLocalString("delete.managed.thread.factory.no.resource-ref", "This managed thread factory [ {0} ] is not referenced in target [ {1} ]", jndiName, target);
                    return new ResourceStatus(ResourceStatus.FAILURE, msg);
                }

                if (resourceUtil.getTargetsReferringResourceRef(jndiName).size() > 1) {
                    String msg = I18N.getLocalString("delete.managed.thread.factory.multiple.resource-refs", "This managed thread factory [ {0} ] is referenced in multiple instance/cluster targets, Use delete-resource-ref on appropriate target", jndiName);
                    return new ResourceStatus(ResourceStatus.FAILURE, msg);
                }
            }
        }

        try {
            // delete resource-ref
            if (!CommandTarget.TARGET_DOMAIN.equals(target)) {
                resourceUtil.deleteResourceRef(jndiName, target);
            }

            // delete managed-thread-factory
            if (ConfigSupport.apply(param -> param.getResources().remove(resource), resources) == null) {
                String msg = I18N.getLocalString("delete.managed.thread.factory.failed", "Managed thread factory {0} deletion failed", jndiName);
                return new ResourceStatus(ResourceStatus.FAILURE, msg);
            }
        } catch(TransactionFailure tfe) {
            String msg = I18N.getLocalString("delete.managed.thread.factory.failed", "Managed thread factory {0} deletion failed ", jndiName);
            ResourceStatus status = new ResourceStatus(ResourceStatus.FAILURE, msg);
            status.setException(tfe);
            return status;
        }

        String msg = I18N.getLocalString("delete.managed.thread.factory.success", "Managed thread factory {0} deleted successfully", jndiName);
        return new ResourceStatus(ResourceStatus.SUCCESS, msg);
    }
}
