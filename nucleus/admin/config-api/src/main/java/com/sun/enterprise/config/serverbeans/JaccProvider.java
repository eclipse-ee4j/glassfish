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

package com.sun.enterprise.config.serverbeans;

import jakarta.validation.constraints.NotNull;

import java.beans.PropertyVetoException;
import java.util.List;

import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.quality.ToDo;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

/**
 * Defines the standard JACC properties used for setting up the JACC provider.
 * It also allows optional properties which can be used by the provider implementation
 * for its configuration.
 */
@Configured
public interface JaccProvider extends ConfigBeanProxy, PropertyBag {

    /**
     * Gets the value of the {@code name} property.
     *
     * <p>A name for this jacc-provider. Is always {@code default} for default provider.
     *
     * @return possible object is {@link String}
     */
    @Attribute(key = true)
    @NotNull
    String getName();

    /**
     * Sets the value of the {@code name} property.
     *
     * @param name allowed object is {@link String}
     */
    void setName(String name) throws PropertyVetoException;

    /**
     * Gets the value of the {@code policyProvider} property.
     *
     * <p>Corresponds to (and can be overridden by) the system property
     * {@code jakarta.security.jacc.policy.provider}.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    @NotNull
    String getPolicyProvider();

    /**
     * Sets the value of the {@code policyProvider} property.
     *
     * @param policyProvider allowed object is {@link String}
     */
    void setPolicyProvider(String policyProvider) throws PropertyVetoException;

    /**
     * Gets the value of the {@code policyConfigurationFactoryProvider} property.
     *
     * <p>Corresponds to (and can be overridden by) the system property
     * {@code jakarta.security.jacc.PolicyConfigurationFactory.provider}.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getPolicyConfigurationFactoryProvider();

    /**
     * Sets the value of the {@code policyConfigurationFactoryProvider} property.
     *
     * @param configurationFactoryProvider allowed object is {@link String}
     */
    void setPolicyConfigurationFactoryProvider(String configurationFactoryProvider) throws PropertyVetoException;

    /**
     * Properties as per {@link PropertyBag}.
     */
    @ToDo(priority = ToDo.Priority.IMPORTANT, details = "Provide PropertyDesc for legal props")
    @PropertiesDesc(props = {})
    @Element
    List<Property> getProperty();
}
