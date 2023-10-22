/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

package org.glassfish.connectors.config;

import com.sun.enterprise.config.serverbeans.Resource;

import jakarta.validation.constraints.Pattern;

import java.beans.PropertyVetoException;
import java.util.List;

import org.glassfish.api.admin.RestRedirect;
import org.glassfish.api.admin.RestRedirects;
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

import static org.glassfish.config.support.Constants.NAME_REGEX;

/**
 * This element is for configuring the resource adapter. These values
 * (properties) over-rides the default values present in ra.xml. The name
 * attribute has to be unique . It is optional for PE. It is used mainly for EE.
 */
@Configured
@RestRedirects({
        @RestRedirect(opType = RestRedirect.OpType.POST, commandName = "create-resource-adapter-config"),
        @RestRedirect(opType = RestRedirect.OpType.DELETE, commandName = "delete-resource-adapter-config")
})
@ResourceTypeOrder(deploymentOrder = ResourceDeploymentOrder.RESOURCEADAPTERCONFIG_RESOURCE)
public interface ResourceAdapterConfig extends ConfigBeanProxy, Resource, PropertyBag {

    String PATTERN_RA_NAME = "[^',][^',\\\\]*";

    /**
     * Gets the value of the {@code name} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    @Pattern(regexp = NAME_REGEX, message = "Pattern: " + NAME_REGEX)
    String getName();

    /**
     * Sets the value of the {@code  name} property.
     *
     * @param name allowed object is {@link String}
     */
    void setName(String name) throws PropertyVetoException;

    /**
     * Gets the value of the {@code threadPoolIds} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getThreadPoolIds();

    /**
     * Sets the value of the {@code threadPoolIds} property.
     *
     * @param poolIds allowed object is {@link String}
     */
    void setThreadPoolIds(String poolIds) throws PropertyVetoException;

    /**
     * Gets the value of the {@code resourceAdapterName} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(key = true)
    @Pattern(regexp = PATTERN_RA_NAME, message = "Pattern: " + PATTERN_RA_NAME)
    String getResourceAdapterName();

    /**
     * Sets the value of the {@code resourceAdapterName} property.
     *
     * @param raName allowed object is {@link String}
     */
    void setResourceAdapterName(String raName) throws PropertyVetoException;

    /**
     * Properties as per {@link org.jvnet.hk2.config.types.PropertyBag}
     */
    @Override
    @ToDo(priority = ToDo.Priority.IMPORTANT, details = "Provide PropertyDesc for legal props")
    @PropertiesDesc(props = {})
    @Element
    List<Property> getProperty();

    @Override
    default String getIdentity() {
        return getResourceAdapterName();
    }
}
