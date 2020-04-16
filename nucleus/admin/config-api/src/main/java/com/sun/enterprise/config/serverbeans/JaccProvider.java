/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.ConfigBeanProxy;

import java.beans.PropertyVetoException;
import java.util.List;

import org.glassfish.api.admin.config.PropertiesDesc;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;
import org.glassfish.quality.ToDo;

import javax.validation.constraints.NotNull;

/**
 * Defines the standard JACC properties used for setting up the JACC provider.
 * It also allows optional properties which can be used by the provider
 * implementation for its configuration.
 */

/* @XmlType(name = "", propOrder = {
    "property"
}) */

@Configured
public interface JaccProvider extends ConfigBeanProxy, PropertyBag {

    /**
     * Gets the value of the name property.
     *
     * A name for this jacc-provider. Is always "default" for default provider.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute(key=true)
    @NotNull
    String getName();

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setName(String value) throws PropertyVetoException;

    /**
     * Gets the value of the policyProvider property.
     *
     * Corresponds to (and can be overridden by) the system property
     * jakarta.security.jacc.policy.provider
     * 
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    @NotNull
    String getPolicyProvider();

    /**
     * Sets the value of the policyProvider property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setPolicyProvider(String value) throws PropertyVetoException;

    /**
     * Gets the value of the policyConfigurationFactoryProvider property.
     *
     * Corresponds to (and can be overridden by) the system property
     * jakarta.security.jacc.PolicyConfigurationFactory.provider
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    String getPolicyConfigurationFactoryProvider();

    /**
     * Sets the value of the policyConfigurationFactoryProvider property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setPolicyConfigurationFactoryProvider(String value) throws PropertyVetoException;
    
    /**
    	Properties as per {@link PropertyBag}
     */
    @ToDo(priority=ToDo.Priority.IMPORTANT, details="Provide PropertyDesc for legal props" )
    @PropertiesDesc(props={})
    @Element
    List<Property> getProperty();
}
