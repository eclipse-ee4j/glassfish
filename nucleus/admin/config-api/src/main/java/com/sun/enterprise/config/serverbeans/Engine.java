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
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

/**
 *
 */
@Configured
public interface Engine extends ConfigBeanProxy, PropertyBag {

    /**
     * Gets the value of the {@code sniffer} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(key = true)
    @NotNull
    String getSniffer();

    /**
     * Sets the value of the {@code sniffer} property.
     *
     * @param sniffer allowed object is {@link String}
     */
    void setSniffer(String sniffer) throws PropertyVetoException;

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

    // TODO: Make this not a list once the hk2/config bug with a single (not list) ("*") is working.
    @Element("*")
    List<ApplicationConfig> getApplicationConfigs();

    // TODO: remove this once hk2/config supports non-list @Element("*").
    default ApplicationConfig getApplicationConfig() {
        List<ApplicationConfig> appConfigs = getApplicationConfigs();
        return (appConfigs.isEmpty()) ? null : appConfigs.get(0);
    }

    // TODO: remove this once hk2/config supports non-list @Element("*").
    default void setApplicationConfig(ApplicationConfig config) {
        List<ApplicationConfig> appConfigs = getApplicationConfigs();
        appConfigs.clear();
        appConfigs.add(config);
    }

    /**
     * Creates a new instance of the specified type of app config.
     *
     * @param <T> stands for the specific type required
     * @param configType the Class for the type required
     * @return new instance of the specified type of ApplicationConfig
     * @throws TransactionFailure if an error occurred
     */
    @SuppressWarnings("unchecked")
    default <T extends ApplicationConfig> T newApplicationConfig(Class<T> configType) throws TransactionFailure {
        return (T) ConfigSupport.apply(e -> {
                T newChild = e.createChild(configType);
                e.getApplicationConfigs().add(newChild);
                return newChild;
        }, this);
    }

    /**
     * Properties as per {@link PropertyBag}
     */
    @ToDo(priority = ToDo.Priority.IMPORTANT, details = "Provide PropertyDesc for legal props")
    @PropertiesDesc(props = {})
    @Element
    List<Property> getProperty();
}
