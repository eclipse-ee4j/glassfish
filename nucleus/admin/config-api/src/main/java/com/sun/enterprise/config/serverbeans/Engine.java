/*
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

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.ConfigBeanProxy;

import java.beans.PropertyVetoException;
import java.util.List;

import org.glassfish.api.admin.config.PropertiesDesc;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

import org.glassfish.quality.ToDo;

import jakarta.validation.constraints.NotNull;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.DuckTyped;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 *
 */

/* @XmlType(name = "", propOrder = {
    "description",
    "property"
}) */

@Configured
public interface Engine extends ConfigBeanProxy, PropertyBag {

    /**
     * Gets the value of the sniffer property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(key = true)
    @NotNull
    String getSniffer();

    /**
     * Sets the value of the sniffer property.
     *
     * @param value allowed object is {@link String }
     */
    void setSniffer(String value) throws PropertyVetoException;

    /**
     * Gets the value of the description property.
     *
     * @return possible object is {@link String }
     */
    @Attribute
    String getDescription();

    /**
     * Sets the value of the description property.
     *
     * @param value allowed object is {@link String }
     */
    void setDescription(String value) throws PropertyVetoException;

    // TODO: Make this not a list once the hk2/config bug with a single (not list) ("*") is working.
    @Element("*")
    List<ApplicationConfig> getApplicationConfigs();

    //    void setConfig(ApplicationConfig config) throws PropertyVetoException;

    // TODO: remove this once hk2/config supports non-list @Element("*").
    @DuckTyped
    ApplicationConfig getApplicationConfig();

    // TODO: remove this once hk2/config supports non-list @Element("*").
    @DuckTyped
    void setApplicationConfig(ApplicationConfig config);

    /**
     * Creates a new instance of the specified type of app config.
     *
     * @param <T> stands for the specific type required
     * @param configType the Class for the type required
     * @return new instance of the specified type of ApplicationConfig
     * @throws TransactionFailure
     */
    @DuckTyped
    <T extends ApplicationConfig> T newApplicationConfig(Class<T> configType) throws TransactionFailure;

    /**
     * Properties as per {@link PropertyBag}
     */
    @ToDo(priority = ToDo.Priority.IMPORTANT, details = "Provide PropertyDesc for legal props")
    @PropertiesDesc(props = {})
    @Element
    List<Property> getProperty();

    // TODO: remove this once hk2/config supports non-list @Element("*").
    class Duck {
        public static ApplicationConfig getApplicationConfig(Engine instance) {
            return (instance.getApplicationConfigs().size() == 0) ? null : instance.getApplicationConfigs().get(0);
        }

        public static void setApplicationConfig(Engine instance, ApplicationConfig config) {
            instance.getApplicationConfigs().clear();
            instance.getApplicationConfigs().add(config);
        }

        public static <T extends ApplicationConfig> T newApplicationConfig(final Engine instance, final Class<T> configType)
                throws TransactionFailure {
            return (T) ConfigSupport.apply(new SingleConfigCode<Engine>() {

                public Object run(Engine e) throws PropertyVetoException, TransactionFailure {
                    T newChild = e.createChild(configType);
                    e.getApplicationConfigs().add(newChild);
                    return newChild;
                }
            }, instance);
        }
    }
}
