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

/**
 * Concurrency managed scheduled executor service resource definition
 */

@Configured
@ResourceConfigCreator(commandName="create-managed-scheduled-executor-service")
@RestRedirects({
 @RestRedirect(opType = RestRedirect.OpType.POST, commandName = "create-managed-scheduled-executor-service"),
 @RestRedirect(opType = RestRedirect.OpType.DELETE, commandName = "delete-managed-scheduled-executor-service")
})
@ResourceTypeOrder(deploymentOrder=ResourceDeploymentOrder.MANAGED_SCHEDULED_EXECUTOR_SERVICE)
@ReferenceConstraint(skipDuringCreation=true, payload=ManagedScheduledExecutorService.class)
@UniqueResourceNameConstraint(message="{resourcename.isnot.unique}", payload=ManagedScheduledExecutorService.class)
@CustomConfiguration(baseConfigurationFileName = "managed-scheduled-executor-service-conf.xml")
public interface ManagedScheduledExecutorService extends ConfigBeanProxy,
        Resource, BindableResource, Payload, ConcurrencyResource, ManagedExecutorServiceBase {

    @DuckTyped
    String getIdentity();

    class Duck {
        public static String getIdentity(
            ManagedScheduledExecutorService resource){
            return resource.getJndiName();
        }
    }

    @Service
    public class ManagedScheduledExecutorServiceConfigActivator extends ConfigBeanInstaller {

    }
}
