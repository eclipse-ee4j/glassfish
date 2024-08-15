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
 * Connects a specific listener class with specific managed objects
 */

/* @XmlType(name = "", propOrder = {
    "property"
}) */

@Configured
public interface ListenerConfig extends ConfigBeanProxy, PropertyBag {

    /**
     * Gets the value of the listenerClassName property.
     *
     * The name of a class that can act as a listener for alerts. Non-empty string containing a Java class name.
     *
     * @return possible object is {@link String }
     */
    @Attribute(key = true)
    @NotNull
    String getListenerClassName();

    /**
     * Sets the value of the listenerClassName property.
     *
     * @param value allowed object is {@link String }
     */
    void setListenerClassName(String value) throws PropertyVetoException;

    /**
     * Gets the value of the subscribeListenerWith property.
     *
     * A list of managed object names that the listener should be subscribed to. A non-empty, comma separated list.
     *
     * @return possible object is {@link String }
     */
    @Attribute
    @NotNull
    String getSubscribeListenerWith();

    /**
     * Sets the value of the subscribeListenerWith property.
     *
     * @param value allowed object is {@link String }
     */
    void setSubscribeListenerWith(String value) throws PropertyVetoException;

    /**
     * Properties as per {@link org.jvnet.hk2.config.types.PropertyBag}
     */
    @ToDo(priority = ToDo.Priority.IMPORTANT, details = "Provide PropertyDesc for legal props")
    @PropertiesDesc(props = {})
    @Element
    List<Property> getProperty();
}
