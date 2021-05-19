/*
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

package org.glassfish.concurrent.config;

import com.sun.enterprise.config.modularity.ConfigBeanInstaller;
import com.sun.enterprise.config.modularity.annotation.CustomConfiguration;
import com.sun.enterprise.config.serverbeans.BindableResource;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.customvalidators.ReferenceConstraint;
import org.glassfish.admin.cli.resources.ResourceConfigCreator;
import org.glassfish.api.admin.RestRedirect;
import org.glassfish.api.admin.RestRedirects;
import org.glassfish.admin.cli.resources.UniqueResourceNameConstraint;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.*;
import org.glassfish.resourcebase.resources.ResourceTypeOrder;
import org.glassfish.resourcebase.resources.ResourceDeploymentOrder;

import jakarta.validation.Payload;
import jakarta.validation.constraints.Min;
import java.beans.PropertyVetoException;

/**
 * Concurrency managed executor service resource definition
 */

@Configured
@ResourceConfigCreator(commandName="create-managed-executor-service")
@RestRedirects({
 @RestRedirect(opType = RestRedirect.OpType.POST, commandName = "create-managed-executor-service"),
 @RestRedirect(opType = RestRedirect.OpType.DELETE, commandName = "delete-managed-executor-service")
})
@ResourceTypeOrder(deploymentOrder=ResourceDeploymentOrder.MANAGED_EXECUTOR_SERVICE)
@ReferenceConstraint(skipDuringCreation=true, payload=ManagedExecutorService.class)
@UniqueResourceNameConstraint(message="{resourcename.isnot.unique}", payload=ManagedExecutorService.class)
@CustomConfiguration(baseConfigurationFileName = "managed-executor-service-conf.xml")
public interface ManagedExecutorService extends ConfigBeanProxy, Resource,
        BindableResource, ConcurrencyResource, ManagedExecutorServiceBase,
        Payload {

    /**
     * Gets the value of the maximumPoolSize property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = ""+Integer.MAX_VALUE, dataType = Integer.class)
    @Min(value=0)
    String getMaximumPoolSize();

    /**
     * Sets the value of the maximumPoolSize property.
     *
     * @param value allowed object is {@link String }
     */
    void setMaximumPoolSize(String value) throws PropertyVetoException;

    /**
     * Gets the value of the threadLifetimeSeconds property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = ""+Integer.MAX_VALUE, dataType = Integer.class)
    @Min(value=0)
    String getTaskQueueCapacity();

    /**
     * Sets the value of the threadLifetimeSeconds property.
     *
     * @param value allowed object is {@link String }
     */
    void setTaskQueueCapacity(String value) throws PropertyVetoException;

    @DuckTyped
    String getIdentity();

    class Duck {
        public static String getIdentity(ManagedExecutorService resource){
            return resource.getJndiName();
        }
    }

    @Service
    public  class ManagedExecutorServiceConfigActivator extends ConfigBeanInstaller {

    }
}
