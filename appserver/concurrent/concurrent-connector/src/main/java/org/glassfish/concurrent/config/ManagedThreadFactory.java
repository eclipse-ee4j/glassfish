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

import java.beans.PropertyVetoException;

import org.glassfish.admin.cli.resources.ResourceConfigCreator;
import org.glassfish.admin.cli.resources.UniqueResourceNameConstraint;
import org.glassfish.api.admin.RestRedirect;
import org.glassfish.api.admin.RestRedirects;
import org.glassfish.resourcebase.resources.ResourceDeploymentOrder;
import org.glassfish.resourcebase.resources.ResourceTypeOrder;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.DuckTyped;

import com.sun.enterprise.config.modularity.ConfigBeanInstaller;
import com.sun.enterprise.config.modularity.annotation.CustomConfiguration;
import com.sun.enterprise.config.serverbeans.BindableResource;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.customvalidators.ReferenceConstraint;

import jakarta.validation.Payload;
import jakarta.validation.constraints.Min;

/**
 * Concurrency managed thread factory resource definition
 */

@Configured
@ResourceConfigCreator(commandName="create-managed-thread-factory")
@RestRedirects({
 @RestRedirect(opType = RestRedirect.OpType.POST, commandName = "create-managed-thread-factory"),
 @RestRedirect(opType = RestRedirect.OpType.DELETE, commandName = "delete-managed-thread-factory")
})
@ResourceTypeOrder(deploymentOrder=ResourceDeploymentOrder.MANAGED_THREAD_FACTORY)
@ReferenceConstraint(skipDuringCreation=true, payload=ManagedThreadFactory.class)
@UniqueResourceNameConstraint(message="{resourcename.isnot.unique}", payload=ManagedThreadFactory.class)
@CustomConfiguration(baseConfigurationFileName = "managed-thread-factory-conf.xml")
public interface ManagedThreadFactory extends ConfigBeanProxy, Resource,
        BindableResource, ConcurrencyResource, Payload  {

    /**
     * Gets the value of the threadPriority property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute(defaultValue=""+Thread.NORM_PRIORITY, dataType=Integer.class)
    @Min(value=0)
    String getThreadPriority();

    /**
     * Sets the value of the threadPriority property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setThreadPriority(String value) throws PropertyVetoException;

    @Override
    @DuckTyped
    String getIdentity();

    /**
     * Gets the value of the context property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "", dataType = String.class)
    String getContext();

    /**
     * Sets the value of the context property.
     *
     * @param value allowed object is {@link String }
     */
    void setContext(String value) throws PropertyVetoException;

    class Duck {
        public static String getIdentity(ManagedThreadFactory resource){
            return resource.getJndiName();
        }
    }

    @Service
    public class ManagedThreadFactoryConfigActivator extends ConfigBeanInstaller {

    }
}
