/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.ServerTags;

import java.beans.PropertyVetoException;
import java.util.Properties;

import org.glassfish.api.I18n;
import org.glassfish.concurrent.config.ManagedExecutorService;
import org.glassfish.concurrent.config.ManagedExecutorServiceBase;
import org.glassfish.resourcebase.resources.api.ResourceStatus;
import org.glassfish.resources.api.ResourceAttributes;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfiguredBy;
import org.jvnet.hk2.config.TransactionFailure;

import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.MAXIMUM_POOL_SIZE;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.TASK_QUEUE_CAPACITY;
import static org.glassfish.resourcebase.resources.api.ResourceStatus.FAILURE;

/**
 *
 * The managed executor service manager allows you to create and delete
 * the managed-executor-service config element
 */
@Service (name=ServerTags.MANAGED_EXECUTOR_SERVICE)
@I18n("managed.executor.service.manager")
@ConfiguredBy(Resources.class)
public class ManagedExecutorServiceManager extends ManagedExecutorServiceBaseManager {

    private String maximumPoolSize = ""+Integer.MAX_VALUE;
    private String taskQueueCapacity = ""+Integer.MAX_VALUE;

    @Override
    protected void setAttributes(ResourceAttributes attributes, String target) {
        super.setAttributes(attributes, target);
        maximumPoolSize = attributes.getString(MAXIMUM_POOL_SIZE);
        taskQueueCapacity = attributes.getString(TASK_QUEUE_CAPACITY);
    }

    @Override
    protected ResourceStatus isValid(Resources resources, boolean validateResourceRef, String target){
        if (Integer.parseInt(corePoolSize) == 0 &&
            Integer.parseInt(maximumPoolSize) == 0) {
            return new ResourceStatus(FAILURE, "Options corepoolsize and maximumpoolsize cannot both have value 0.");
        }

        if (Integer.parseInt(corePoolSize) >
            Integer.parseInt(maximumPoolSize)) {
            return new ResourceStatus(FAILURE, "Option corepoolsize cannot have a bigger value than option maximumpoolsize.");
        }

        return super.isValid(resources, validateResourceRef, target);
    }

    @Override
    protected ManagedExecutorServiceBase createConfigBean(Resources param, Properties properties) throws PropertyVetoException, TransactionFailure {
        ManagedExecutorService managedExecutorService = param.createChild(ManagedExecutorService.class);
        setAttributesOnConfigBean(managedExecutorService, properties);
        managedExecutorService.setMaximumPoolSize(maximumPoolSize);
        managedExecutorService.setTaskQueueCapacity(taskQueueCapacity);
        return managedExecutorService;
    }

    @Override
    public String getResourceType () {
        return ServerTags.MANAGED_EXECUTOR_SERVICE;
    }
}
