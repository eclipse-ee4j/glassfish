/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.util.LocalStringManagerImpl;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.concurrent.config.ManagedExecutorServiceBase;
import org.glassfish.concurrent.config.ManagedExecutorService;
import org.glassfish.concurrent.config.ManagedScheduledExecutorService;
import org.glassfish.resources.admin.cli.ResourceManager;
import org.glassfish.resourcebase.resources.admin.cli.ResourceUtil;
import org.glassfish.resourcebase.resources.api.ResourceStatus;
import org.glassfish.resourcebase.resources.util.BindableResourcesHelper;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

import jakarta.inject.Inject;
import jakarta.resource.ResourceException;
import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.glassfish.resources.admin.cli.ResourceConstants.*;

/**
 *
 * The base managed executor service manager for managed executor service
 * and managed scheduled executor service
 */
public abstract class ManagedExecutorServiceBaseManager implements ResourceManager {

    protected final static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(ManagedExecutorServiceBaseManager.class);
    protected static final String DESCRIPTION = ServerTags.DESCRIPTION;
    protected String jndiName = null;
    protected String description = null;
    protected String threadPriority = ""+Thread.NORM_PRIORITY;
    protected String contextInfoEnabled = Boolean.TRUE.toString();
    protected String contextInfo = CONTEXT_INFO_DEFAULT_VALUE;
    protected String longRunningTasks = Boolean.FALSE.toString();
    protected String hungAfterSeconds = "0";
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

    public abstract String getResourceType();

    public ResourceStatus create(Resources resources, HashMap attributes, final Properties properties,
                                 String target) throws Exception {

        setAttributes(attributes, target);

        ResourceStatus validationStatus = isValid(resources, true, target);
        if(validationStatus.getStatus() == ResourceStatus.FAILURE){
            return validationStatus;
        }

        try {
            ConfigSupport.apply(new SingleConfigCode<Resources>() {

                public Object run(Resources param) throws PropertyVetoException, TransactionFailure {
                    return createResource(param, properties);
                }
            }, resources);

                resourceUtil.createResourceRef(jndiName, enabledValueForTarget, target);
        } catch (TransactionFailure tfe) {
             String msg = localStrings.getLocalString("create.managed.executor.service.failed", "Managed executor service {0} creation failed", jndiName) + " " + tfe.getLocalizedMessage();
            if (getResourceType().equals(ServerTags.MANAGED_SCHEDULED_EXECUTOR_SERVICE)) {
                msg = localStrings.getLocalString("create.managed.scheduled.executor.service.failed", "Managed scheduled executor service {0} creation failed", jndiName) + " " + tfe.getLocalizedMessage();
            }
            ResourceStatus status = new ResourceStatus(ResourceStatus.FAILURE, msg);
            status.setException(tfe);
            return status;
        }
        String msg = localStrings.getLocalString("create.managed.executor.service.success", "Managed executor service {0} created successfully", jndiName);
        if (getResourceType().equals(ServerTags.MANAGED_SCHEDULED_EXECUTOR_SERVICE)) {
            msg = localStrings.getLocalString("create.managed.scheduled.executor.service.success", "Managed scheduled executor service {0} created successfully", jndiName);
        }
        return new ResourceStatus(ResourceStatus.SUCCESS, msg);
    }

    protected ResourceStatus isValid(Resources resources, boolean validateResourceRef, String target){
        ResourceStatus status ;


        if (jndiName == null) {
            String msg = localStrings.getLocalString("managed.executor.service.noJndiName", "No JNDI name defined for managed executor service.");
            if (getResourceType().equals(ServerTags.MANAGED_SCHEDULED_EXECUTOR_SERVICE)) {
                msg = localStrings.getLocalString("managed.scheduled.executor.service.noJndiName", "No JNDI name defined for managed scheduled executor service.");
            }
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }

        Class clazz = ManagedExecutorService.class;
        if (getResourceType().equals(ServerTags.MANAGED_SCHEDULED_EXECUTOR_SERVICE)) {
            clazz = ManagedScheduledExecutorService.class;
        }
        status = resourcesHelper.validateBindableResourceForDuplicates(resources, jndiName, validateResourceRef, target, clazz);

        return status;
    }

    protected void setAttributes(HashMap attributes, String target) {
        jndiName = (String) attributes.get(JNDI_NAME);
        description = (String) attributes.get(DESCRIPTION);
        contextInfo = (String) attributes.get(CONTEXT_INFO);
        contextInfoEnabled = (String) attributes.get(CONTEXT_INFO_ENABLED);
        threadPriority = (String) attributes.get(THREAD_PRIORITY);
        longRunningTasks = (String) attributes.get(LONG_RUNNING_TASKS);
        hungAfterSeconds = (String) attributes.get(HUNG_AFTER_SECONDS);
        hungLoggerInitialDelaySeconds = (String) attributes.get(HUNG_LOGGER_INITIAL_DELAY_SECONDS);
        hungLoggerIntervalSeconds = (String) attributes.get(HUNG_LOGGER_INTERVAL_SECONDS);
        corePoolSize = (String) attributes.get(CORE_POOL_SIZE);
        keepAliveSeconds = (String) attributes.get(KEEP_ALIVE_SECONDS);
        threadLifetimeSeconds = (String) attributes.get(THREAD_LIFETIME_SECONDS);
        if(target != null){
            enabled = resourceUtil.computeEnabledValueForResourceBasedOnTarget((String)attributes.get(ENABLED), target);
        }else{
            enabled = (String) attributes.get(ENABLED);
        }
        enabledValueForTarget = (String) attributes.get(ENABLED);
    }

    protected ManagedExecutorServiceBase createResource(Resources param, Properties properties) throws PropertyVetoException, TransactionFailure {
        ManagedExecutorServiceBase newResource = createConfigBean(param, properties);
        param.getResources().add(newResource);
        return newResource;
    }

    protected abstract ManagedExecutorServiceBase createConfigBean(Resources param, Properties properties) throws PropertyVetoException,TransactionFailure;

    protected void setAttributesOnConfigBean(ManagedExecutorServiceBase managedExecutorService, Properties properties) throws PropertyVetoException, TransactionFailure {
        managedExecutorService.setJndiName(jndiName);
        if (description != null) {
            managedExecutorService.setDescription(description);
        }
        managedExecutorService.setContextInfoEnabled(contextInfoEnabled);
        managedExecutorService.setContextInfo(contextInfo);
        managedExecutorService.setThreadPriority(threadPriority);
        managedExecutorService.setHungAfterSeconds(hungAfterSeconds);
        managedExecutorService.setHungLoggerInitialDelaySeconds(hungLoggerInitialDelaySeconds);
        managedExecutorService.setHungLoggerIntervalSeconds(hungLoggerIntervalSeconds);
        managedExecutorService.setCorePoolSize(corePoolSize);
        managedExecutorService.setKeepAliveSeconds(keepAliveSeconds);
        managedExecutorService.setThreadLifetimeSeconds(threadLifetimeSeconds);
        managedExecutorService.setEnabled(enabled);
        //Fix for GLASSFISH-21251
        managedExecutorService.setLongRunningTasks(longRunningTasks);
        //end of fix GLASSFISH-21251
        if (properties != null) {
            for ( Map.Entry e : properties.entrySet()) {
                Property prop = managedExecutorService.createChild(Property.class);
                prop.setName((String)e.getKey());
                prop.setValue((String)e.getValue());
                managedExecutorService.getProperty().add(prop);
            }
        }
    }

    public Resource createConfigBean(final Resources resources, HashMap attributes, final Properties properties, boolean validate) throws Exception{
        setAttributes(attributes, null);
        ResourceStatus status = null;
        if(!validate){
            status = new ResourceStatus(ResourceStatus.SUCCESS,"");
        }else{
            status = isValid(resources, false, null);
        }
        if(status.getStatus() == ResourceStatus.SUCCESS){
            return createConfigBean(resources, properties);
        }else{
            throw new ResourceException(status.getMessage());
        }
    }

    public ResourceStatus delete (final Resources resources, final String jndiName, final String target)
            throws Exception {

        if (jndiName == null) {
            String msg = localStrings.getLocalString("managed.executor.service.noJndiName", "No JNDI name defined for managed executor service.");
            if (getResourceType().equals(ServerTags.MANAGED_SCHEDULED_EXECUTOR_SERVICE)) {
                msg = localStrings.getLocalString("managed.scheduled.executor.service.noJndiName", "No JNDI name defined for managed scheduled executor service.");
            }

            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }

        Resource resource = null;
        if (getResourceType().equals(ServerTags.MANAGED_EXECUTOR_SERVICE)) {
            resource = ConnectorsUtil.getResourceByName(resources, ManagedExecutorService.class, jndiName);
        } else if (getResourceType().equals(ServerTags.MANAGED_SCHEDULED_EXECUTOR_SERVICE)) {
            resource = ConnectorsUtil.getResourceByName(resources, ManagedScheduledExecutorService.class, jndiName);
        }

        // ensure we already have this resource
        if (resource == null){
            String msg = localStrings.getLocalString("delete.managed.executor.service.notfound", "A managed executor service named {0} does not exist.", jndiName);
            if (getResourceType().equals(ServerTags.MANAGED_SCHEDULED_EXECUTOR_SERVICE)) {
                msg = localStrings.getLocalString("delete.managed.scheduled.executor.service.notfound", "A managed scheduled executor service named {0} does not exist.", jndiName);
            }
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }

        if (SYSTEM_ALL_REQ.equals(resource.getObjectType())) {
            String msg = localStrings.getLocalString("delete.concurrent.resource.notAllowed", "The {0} resource cannot be deleted as it is required to be configured in the system.", jndiName);
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }

        if (environment.isDas()) {

            if ("domain".equals(target)) {
                if (resourceUtil.getTargetsReferringResourceRef(jndiName).size() > 0) {
                    String msg = localStrings.getLocalString("delete.managed.executor.service.resource-ref.exist", "This managed executor service [ {0} ] is referenced in an instance/cluster target, use delete-resource-ref on appropriate target", jndiName);
                    if (getResourceType().equals(ServerTags.MANAGED_SCHEDULED_EXECUTOR_SERVICE)) {
                        msg = localStrings.getLocalString("delete.managed.scheduled.executor.service.resource-ref.exist", "This managed scheduled executor service [ {0} ] is referenced in an instance/cluster target, use delete-resource-ref on appropriate target", jndiName);
                    }
                    return new ResourceStatus(ResourceStatus.FAILURE, msg);
                }
            } else {
                if (!resourceUtil.isResourceRefInTarget(jndiName, target)) {
                    String msg = localStrings.getLocalString("delete.managed.executor.service.no.resource-ref", "This managed executor service [ {0} ] is not referenced in target [ {1} ]", jndiName, target);
                    if (getResourceType().equals(ServerTags.MANAGED_SCHEDULED_EXECUTOR_SERVICE)) {
                        msg = localStrings.getLocalString("delete.managed.scheduled.executor.service.no.resource-ref", "This managed scheduled executor service [ {0} ] is not referenced in target [ {1} ]", jndiName, target);
                    }
                    return new ResourceStatus(ResourceStatus.FAILURE, msg);
                }

                if (resourceUtil.getTargetsReferringResourceRef(jndiName).size() > 1) {
                    String msg = localStrings.getLocalString("delete.managed.executor.service.multiple.resource-refs", "This managed executor service [ {0} ] is referenced in multiple instance/cluster targets, Use delete-resource-ref on appropriate target", jndiName);
                    if (getResourceType().equals(ServerTags.MANAGED_SCHEDULED_EXECUTOR_SERVICE)) {
                        msg = localStrings.getLocalString("delete.managed.scheduled.executor.service.multiple.resource-refs", "This managed scheduled executor service [ {0} ] is referenced in multiple instance/cluster targets, Use delete-resource-ref on appropriate target", jndiName);
                    }
                    return new ResourceStatus(ResourceStatus.FAILURE, msg);
                }
            }
        }

        try {
            // delete resource-ref
            resourceUtil.deleteResourceRef(jndiName, target);

            // delete managed executor service
            if (ConfigSupport.apply(new SingleConfigCode<Resources>() {
                public Object run(Resources param) throws PropertyVetoException, TransactionFailure {
                    ManagedExecutorServiceBase resource = null;
                    if (getResourceType().equals(ServerTags.MANAGED_EXECUTOR_SERVICE)) {
                        resource = (ManagedExecutorService) ConnectorsUtil.getResourceByName(resources, ManagedExecutorService.class, jndiName);
                    } else {
                        resource = (ManagedScheduledExecutorService) ConnectorsUtil.getResourceByName(resources, ManagedScheduledExecutorService.class, jndiName);
                    }
                    return param.getResources().remove(resource);
                }
            }, resources) == null) {
                String msg = localStrings.getLocalString("delete.managed.executor.service.failed", "Managed executor service {0} deletion failed", jndiName);
                if (getResourceType().equals(ServerTags.MANAGED_SCHEDULED_EXECUTOR_SERVICE)) {
                    msg = localStrings.getLocalString("delete.managed.scheduled.executor.service.failed", "Managed scheduled executor service {0} deletion failed", jndiName);
                }
                return new ResourceStatus(ResourceStatus.FAILURE, msg);
            }
        } catch(TransactionFailure tfe) {
            String msg = localStrings.getLocalString("delete.managed.executor.service.failed", "Managed executor service {0} deletion failed ", jndiName);
            if (getResourceType().equals(ServerTags.MANAGED_SCHEDULED_EXECUTOR_SERVICE)) {
                msg = localStrings.getLocalString("delete.managed.scheduled.executor.service.failed", "Managed scheduled executor service {0} deletion failed ", jndiName);
            }
            ResourceStatus status = new ResourceStatus(ResourceStatus.FAILURE, msg);
            status.setException(tfe);
            return status;
        }

        String msg = localStrings.getLocalString("delete.managed.executor.service.success", "Managed executor service {0} deleted successfully", jndiName);
        if (getResourceType().equals(ServerTags.MANAGED_SCHEDULED_EXECUTOR_SERVICE)) {
            msg = localStrings.getLocalString("delete.managed.scheduled.executor.service.success", "Managed scheduled executor service {0} deleted successfully", jndiName);
        }
        return new ResourceStatus(ResourceStatus.SUCCESS, msg);
    }
}
