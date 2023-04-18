/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.resources.config;

import com.sun.enterprise.config.serverbeans.BindableResource;
import com.sun.enterprise.config.serverbeans.Resource;

import jakarta.validation.constraints.NotNull;

import java.beans.PropertyVetoException;
import java.util.List;

import org.glassfish.admin.cli.resources.ResourceConfigCreator;
import org.glassfish.admin.cli.resources.UniqueResourceNameConstraint;
import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.quality.ToDo;
import org.glassfish.resourcebase.resources.ResourceDeploymentOrder;
import org.glassfish.resourcebase.resources.ResourceTypeOrder;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

/**
 * Resource residing in an external JNDI repository.
 */
@Configured
@ResourceConfigCreator(commandName = "create-jndi-resource")
@UniqueResourceNameConstraint(message = "{resourcename.isnot.unique}", payload = ExternalJndiResource.class)
@ResourceTypeOrder(deploymentOrder = ResourceDeploymentOrder.EXTERNALJNDI_RESOURCE)
public interface ExternalJndiResource extends ConfigBeanProxy, Resource, PropertyBag, BindableResource {

    /**
     * Gets the value of the {@code jndiLookupName} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    @NotNull
    String getJndiLookupName();

    /**
     * Sets the value of the {@code jndiLookupName} property.
     *
     * @param lookupName allowed object is {@link String}
     */
    void setJndiLookupName(String lookupName) throws PropertyVetoException;

    /**
     * Gets the value of the {@code resType} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    @NotNull
    String getResType();

    /**
     * Sets the value of the {@code resType} property.
     *
     * @param resType allowed object is {@link String}
     */
    void setResType(String resType) throws PropertyVetoException;

    /**
     * Gets the value of the {@code factoryClass} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    @NotNull
    String getFactoryClass();

    /**
     * Sets the value of the {@code factoryClass} property.
     *
     * @param factoryClass allowed object is {@link String}
     */
    void setFactoryClass(String factoryClass) throws PropertyVetoException;

    /**
     * Gets the value of the {@code enabled} property.
     *
     * @return possible object is {@link String}
     */
    @Override
    @Attribute (defaultValue = "true", dataType = Boolean.class)
    String getEnabled();

    /**
     * Sets the value of the {@code enabled} property.
     *
     * @param enabled allowed object is {@link String}
     */
    @Override
    void setEnabled(String enabled) throws PropertyVetoException;

    /**
     * Gets the value of the {@code description} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getDescription();

    /**
     * Sets the value of the {@code description} property.
     *
     * @param description allowed object is {@link String}
     */
    void setDescription(String description) throws PropertyVetoException;

    /**
     * Properties as per .{@link PropertyBag}
     */
    @Override
    @ToDo(priority = ToDo.Priority.IMPORTANT, details = "Provide PropertyDesc for legal props" )
    @PropertiesDesc(props = {})
    @Element
    List<Property> getProperty();

    @Override
    default String getIdentity() {
        return getJndiName();
    }
}
