/*
 * Copyright (c) 2021, 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.config.serverbeans.BindableResource;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;
import jakarta.resource.ResourceException;

import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.concurrent.config.ManagedExecutorService;
import org.glassfish.concurrent.config.ManagedExecutorServiceBase;
import org.glassfish.concurrent.config.ManagedScheduledExecutorService;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.resourcebase.resources.admin.cli.ResourceUtil;
import org.glassfish.resourcebase.resources.api.ResourceStatus;
import org.glassfish.resourcebase.resources.util.BindableResourcesHelper;
import org.glassfish.resources.admin.cli.ResourceManager;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

import static com.sun.enterprise.config.serverbeans.ServerTags.DESCRIPTION;
import static com.sun.enterprise.config.serverbeans.ServerTags.MANAGED_EXECUTOR_SERVICE;
import static com.sun.enterprise.config.serverbeans.ServerTags.MANAGED_SCHEDULED_EXECUTOR_SERVICE;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.CONTEXT_INFO;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.CONTEXT_INFO_DEFAULT_VALUE;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.CONTEXT_INFO_ENABLED;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.CORE_POOL_SIZE;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.HUNG_AFTER_SECONDS;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.HUNG_LOGGER_INITIAL_DELAY_SECONDS;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.HUNG_LOGGER_INTERVAL_SECONDS;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.HUNG_LOGGER_PRINT_ONCE;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.KEEP_ALIVE_SECONDS;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.LONG_RUNNING_TASKS;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.THREAD_LIFETIME_SECONDS;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.THREAD_PRIORITY;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.VIRTUAL;
import static org.glassfish.resources.admin.cli.ResourceConstants.ENABLED;
import static org.glassfish.resources.admin.cli.ResourceConstants.JNDI_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.SYSTEM_ALL_REQ;

/**
 *
 * The base managed executor service manager for managed executor service
 * and managed scheduled executor service
 */
public abstract class ManagedExecutorServiceBaseManager implements ResourceManager {

    protected static final LocalStringManagerImpl I18N = new LocalStringManagerImpl(
        ManagedExecutorServiceBaseManager.class);
    protected String jndiName;
    protected String description;
    protected String threadPriority = Integer.toString(Thread.NORM_PRIORITY);
    protected String contextInfoEnabled = Boolean.TRUE.toString();
    protected String contextInfo = CONTEXT_INFO_DEFAULT_VALUE;
    protected String virtual = Boolean.FALSE.toString();
    protected String longRunningTasks = Boolean.FALSE.toString();
    protected String hungAfterSeconds = "0";
    protected String hungLoggerPrintOnce = Boolean.FALSE.toString();
    protected String hungLoggerInitialDelaySeconds = "60";
    protected String hungLoggerIntervalSeconds = "60";
    protected String corePoolSize = "0";
    protected String keepAliveSeconds = "60";
    protected String threadLifetimeSeconds = "0";
    protected String enabled = Boolean.TRUE.toString();
    protected String enabledValueForTarget = Boolean.TRUE.toString();

    @Inject
    protected ResourceUtil resourceUtil;
    @Inject
    protected ServerEnvironment environment;
    @Inject
    protected BindableResourcesHelper resourcesHelper;


    @Override
    public abstract String getResourceType();


    @Override
    public ResourceStatus create(Resources resources, HashMap attributes, final Properties properties, String target)
        throws Exception {
        setAttributes(attributes, target);

        ResourceStatus validationStatus = isValid(resources, true, target);
        if(validationStatus.getStatus() == ResourceStatus.FAILURE){
            return validationStatus;
        }

        try {
            ConfigSupport.apply(param -> createResource(param, properties), resources);
            if (!CommandTarget.TARGET_DOMAIN.equals(target)) {
                resourceUtil.createResourceRef(jndiName, enabledValueForTarget, target);
            }
        } catch (TransactionFailure tfe) {
            String msg = I18N.getLocalString("create.managed.executor.service.failed",
                "Managed executor service {0} creation failed", jndiName) + tfe.getLocalizedMessage();
            if (MANAGED_SCHEDULED_EXECUTOR_SERVICE.equals(getResourceType())) {
                msg = I18N.getLocalString("create.managed.scheduled.executor.service.failed",
                    "Managed scheduled executor service {0} creation failed", jndiName) + tfe.getLocalizedMessage();
            }
            ResourceStatus status = new ResourceStatus(ResourceStatus.FAILURE, msg);
            status.setException(tfe);
            return status;
        }
        String msg = I18N.getLocalString("create.managed.executor.service.success",
            "Managed executor service {0} created successfully", jndiName);
        if (MANAGED_SCHEDULED_EXECUTOR_SERVICE.equals(getResourceType())) {
            msg = I18N.getLocalString("create.managed.scheduled.executor.service.success",
                "Managed scheduled executor service {0} created successfully", jndiName);
        }
        return new ResourceStatus(ResourceStatus.SUCCESS, msg);
    }


    protected ResourceStatus isValid(Resources resources, boolean validateResourceRef, String target){
        if (jndiName == null) {
            String msg = I18N.getLocalString("managed.executor.service.noJndiName",
                "No JNDI name defined for managed executor service.");
            if (MANAGED_SCHEDULED_EXECUTOR_SERVICE.equals(getResourceType())) {
                msg = I18N.getLocalString("managed.scheduled.executor.service.noJndiName",
                    "No JNDI name defined for managed scheduled executor service.");
            }
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }
        final Class<? extends BindableResource> clazz;
        if (MANAGED_SCHEDULED_EXECUTOR_SERVICE.equals(getResourceType())) {
            clazz = ManagedScheduledExecutorService.class;
        } else {
            clazz = ManagedExecutorService.class;
        }
        return resourcesHelper.validateBindableResourceForDuplicates(resources, jndiName, validateResourceRef, target, clazz);
    }


    protected void setAttributes(Map<String, String> attributes, String target) {
        jndiName = attributes.get(JNDI_NAME);
        description = attributes.get(DESCRIPTION);
        contextInfo = attributes.get(CONTEXT_INFO);
        contextInfoEnabled = attributes.get(CONTEXT_INFO_ENABLED);
        threadPriority = attributes.get(THREAD_PRIORITY);
        virtual = attributes.get(VIRTUAL);
        longRunningTasks = attributes.get(LONG_RUNNING_TASKS);
        hungAfterSeconds = attributes.get(HUNG_AFTER_SECONDS);
        hungLoggerPrintOnce = attributes.get(HUNG_LOGGER_PRINT_ONCE);
        hungLoggerInitialDelaySeconds = attributes.get(HUNG_LOGGER_INITIAL_DELAY_SECONDS);
        hungLoggerIntervalSeconds = attributes.get(HUNG_LOGGER_INTERVAL_SECONDS);
        corePoolSize = attributes.get(CORE_POOL_SIZE);
        keepAliveSeconds = attributes.get(KEEP_ALIVE_SECONDS);
        threadLifetimeSeconds = attributes.get(THREAD_LIFETIME_SECONDS);
        if (target == null) {
            enabled = attributes.get(ENABLED);
        } else {
            enabled = resourceUtil.computeEnabledValueForResourceBasedOnTarget(attributes.get(ENABLED), target);
        }
        enabledValueForTarget = attributes.get(ENABLED);
    }


    protected ManagedExecutorServiceBase createResource(Resources param, Properties properties)
        throws PropertyVetoException, TransactionFailure {
        ManagedExecutorServiceBase newResource = createConfigBean(param, properties);
        param.getResources().add(newResource);
        return newResource;
    }


    protected abstract ManagedExecutorServiceBase createConfigBean(Resources param, Properties properties)
        throws PropertyVetoException, TransactionFailure;


    protected void setAttributesOnConfigBean(ManagedExecutorServiceBase managedExecutorService, Properties properties) throws PropertyVetoException, TransactionFailure {
        managedExecutorService.setJndiName(jndiName);
        if (description != null) {
            managedExecutorService.setDescription(description);
        }
        managedExecutorService.setContextInfoEnabled(contextInfoEnabled);
        managedExecutorService.setContextInfo(contextInfo);
        managedExecutorService.setThreadPriority(threadPriority);
        managedExecutorService.setHungAfterSeconds(hungAfterSeconds);
        managedExecutorService.setHungLoggerPrintOnce(hungLoggerPrintOnce);
        managedExecutorService.setHungLoggerInitialDelaySeconds(hungLoggerInitialDelaySeconds);
        managedExecutorService.setHungLoggerIntervalSeconds(hungLoggerIntervalSeconds);
        managedExecutorService.setCorePoolSize(corePoolSize);
        managedExecutorService.setKeepAliveSeconds(keepAliveSeconds);
        managedExecutorService.setThreadLifetimeSeconds(threadLifetimeSeconds);
        managedExecutorService.setEnabled(enabled);
        managedExecutorService.setLongRunningTasks(longRunningTasks);
        managedExecutorService.setVirtual(virtual);
        if (properties != null) {
            for (Entry<Object, Object> e : properties.entrySet()) {
                Property prop = managedExecutorService.createChild(Property.class);
                prop.setName((String)e.getKey());
                prop.setValue((String)e.getValue());
                managedExecutorService.getProperty().add(prop);
            }
        }
    }


    @Override
    public Resource createConfigBean(final Resources resources, HashMap attributes, final Properties properties,
        boolean validate) throws Exception {
        setAttributes(attributes, null);
        ResourceStatus status = null;
        if (validate) {
            status = isValid(resources, false, null);
        } else {
            status = new ResourceStatus(ResourceStatus.SUCCESS, "");
        }
        if (status.getStatus() == ResourceStatus.SUCCESS) {
            return createConfigBean(resources, properties);
        }
        throw new ResourceException(status.getMessage());
    }


    public ResourceStatus delete(final Resources resources, final String jndiName, final String target)
        throws Exception {
        if (jndiName == null) {
            String msg = I18N.getLocalString("managed.executor.service.noJndiName",
                "No JNDI name defined for managed executor service.");
            if (MANAGED_SCHEDULED_EXECUTOR_SERVICE.equals(getResourceType())) {
                msg = I18N.getLocalString("managed.scheduled.executor.service.noJndiName",
                    "No JNDI name defined for managed scheduled executor service.");
            }
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }

        final Resource resource;
        SimpleJndiName simpleJndiName = new SimpleJndiName(jndiName);
        if (MANAGED_EXECUTOR_SERVICE.equals(getResourceType())) {
            resource = resources.getResourceByName(ManagedExecutorService.class, simpleJndiName);
        } else if (MANAGED_SCHEDULED_EXECUTOR_SERVICE.equals(getResourceType())) {
            resource = resources.getResourceByName(ManagedScheduledExecutorService.class, simpleJndiName);
        } else {
            resource = null;
        }

        // ensure we already have this resource
        if (resource == null) {
            String msg = I18N.getLocalString("delete.managed.executor.service.notfound",
                "A managed executor service named {0} does not exist.", jndiName);
            if (MANAGED_SCHEDULED_EXECUTOR_SERVICE.equals(getResourceType())) {
                msg = I18N.getLocalString("delete.managed.scheduled.executor.service.notfound",
                    "A managed scheduled executor service named {0} does not exist.", jndiName);
            }
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }

        if (SYSTEM_ALL_REQ.equals(resource.getObjectType())) {
            String msg = I18N.getLocalString("delete.concurrent.resource.notAllowed",
                "The {0} resource cannot be deleted as it is required to be configured in the system.", jndiName);
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }

        if (environment.isDas()) {
            if (CommandTarget.TARGET_DOMAIN.equals(target)) {
                if (!resourceUtil.getTargetsReferringResourceRef(simpleJndiName).isEmpty()) {
                    String msg = I18N.getLocalString("delete.managed.executor.service.resource-ref.exist",
                        "This managed executor service [ {0} ] is referenced in an instance/cluster target,"
                            + " use delete-resource-ref on appropriate target",
                        jndiName);
                    if (MANAGED_SCHEDULED_EXECUTOR_SERVICE.equals(getResourceType())) {
                        msg = I18N.getLocalString("delete.managed.scheduled.executor.service.resource-ref.exist",
                            "This managed scheduled executor service [ {0} ] is referenced in an instance/cluster"
                                + " target, use delete-resource-ref on appropriate target",
                            jndiName);
                    }
                    return new ResourceStatus(ResourceStatus.FAILURE, msg);
                }
            } else {
                if (!resourceUtil.isResourceRefInTarget(simpleJndiName, target)) {
                    String msg = I18N.getLocalString("delete.managed.executor.service.no.resource-ref",
                        "This managed executor service [ {0} ] is not referenced in target [ {1} ]", jndiName, target);
                    if (MANAGED_SCHEDULED_EXECUTOR_SERVICE.equals(getResourceType())) {
                        msg = I18N.getLocalString("delete.managed.scheduled.executor.service.no.resource-ref",
                            "This managed scheduled executor service [ {0} ] is not referenced in target [ {1} ]",
                            jndiName, target);
                    }
                    return new ResourceStatus(ResourceStatus.FAILURE, msg);
                }

                if (resourceUtil.getTargetsReferringResourceRef(simpleJndiName).size() > 1) {
                    String msg = I18N.getLocalString("delete.managed.executor.service.multiple.resource-refs",
                        "This managed executor service [ {0} ] is referenced in multiple instance/cluster targets,"
                            + " Use delete-resource-ref on appropriate target",
                        jndiName);
                    if (MANAGED_SCHEDULED_EXECUTOR_SERVICE.equals(getResourceType())) {
                        msg = I18N.getLocalString("delete.managed.scheduled.executor.service.multiple.resource-refs",
                            "This managed scheduled executor service [ {0} ] is referenced in multiple instance/cluster"
                                + " targets, Use delete-resource-ref on appropriate target",
                            jndiName);
                    }
                    return new ResourceStatus(ResourceStatus.FAILURE, msg);
                }
            }
        }

        try {
            if (!CommandTarget.TARGET_DOMAIN.equals(target)) {
                resourceUtil.deleteResourceRef(simpleJndiName, target);
            }

            // delete managed executor service
            SingleConfigCode<Resources> configCode = param -> {
                ManagedExecutorServiceBase removedResource = null;
                if (MANAGED_EXECUTOR_SERVICE.equals(getResourceType())) {
                    removedResource = resources.getResourceByName(ManagedExecutorService.class, simpleJndiName);
                } else {
                    removedResource = resources.getResourceByName(ManagedScheduledExecutorService.class, simpleJndiName);
                }
                return param.getResources().remove(removedResource);
            };
            if (ConfigSupport.apply(configCode, resources) == null) {
                String msg = I18N.getLocalString("delete.managed.executor.service.failed",
                    "Managed executor service {0} deletion failed", jndiName);
                if (MANAGED_SCHEDULED_EXECUTOR_SERVICE.equals(getResourceType())) {
                    msg = I18N.getLocalString("delete.managed.scheduled.executor.service.failed",
                        "Managed scheduled executor service {0} deletion failed", jndiName);
                }
                return new ResourceStatus(ResourceStatus.FAILURE, msg);
            }
        } catch (TransactionFailure tfe) {
            String msg = I18N.getLocalString("delete.managed.executor.service.failed",
                "Managed executor service {0} deletion failed ", jndiName);
            if (MANAGED_SCHEDULED_EXECUTOR_SERVICE.equals(getResourceType())) {
                msg = I18N.getLocalString("delete.managed.scheduled.executor.service.failed",
                    "Managed scheduled executor service {0} deletion failed ", jndiName);
            }
            ResourceStatus status = new ResourceStatus(ResourceStatus.FAILURE, msg);
            status.setException(tfe);
            return status;
        }

        String msg = I18N.getLocalString("delete.managed.executor.service.success",
            "Managed executor service {0} deleted successfully", jndiName);
        if (MANAGED_SCHEDULED_EXECUTOR_SERVICE.equals(getResourceType())) {
            msg = I18N.getLocalString("delete.managed.scheduled.executor.service.success",
                "Managed scheduled executor service {0} deleted successfully", jndiName);
        }
        return new ResourceStatus(ResourceStatus.SUCCESS, msg);
    }
}
