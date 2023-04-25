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

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.beans.PropertyVetoException;
import java.util.List;

import org.glassfish.resourcebase.resources.ResourceDeploymentOrder;
import org.glassfish.resourcebase.resources.ResourceTypeOrder;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;

@Configured
@ResourceTypeOrder(deploymentOrder = ResourceDeploymentOrder.WORKSECURITYMAP_RESOURCE)
public interface WorkSecurityMap extends ConfigBeanProxy, Resource {

    String PATTERN_RA_NAME = "[^',][^',\\\\]*";

    /**
     * Gets the value of the {@code enabled} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "true", dataType = Boolean.class)
    String getEnabled();

    /**
     * Sets the value of the {@code enabled} property.
     *
     * @param enabled allowed object is {@link String}
     */
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
     * Gets the value of the ra name.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    @NotNull
    @Pattern(regexp = PATTERN_RA_NAME, message = "Pattern: " + PATTERN_RA_NAME)
    String getResourceAdapterName();

    /**
     * Sets the value of the ra name.
     *
     * @param name allowed object is {@link String}
     */
    void setResourceAdapterName(String name) throws PropertyVetoException;

    /**
     * Gets the group map.
     *
     * @return group map
     */
    @Element
    @NotNull
    List<GroupMap> getGroupMap();

    /**
     * Gets the principal map
     *
     * @return principal map
     */
    @Element
    @NotNull
    List<PrincipalMap> getPrincipalMap();

    /**
     * Name of the configured object.
     *
     * @return name of the configured object
     */
    @Attribute(required = true, key = true)
    @Pattern(regexp = PATTERN_RA_NAME, message = "Pattern: " + PATTERN_RA_NAME)
    @NotNull
    String getName();

    void setName(String name) throws PropertyVetoException;

    @Override
    default String getIdentity() {
        return ("resource-adapter : " + getResourceAdapterName() + " : security-map : " + getName());
    }
}
