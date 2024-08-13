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

import org.glassfish.api.admin.config.Named;
import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.quality.ToDo;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

/**
 * Note on the Name of the MBean : It is a String that represents the name of the MBean. It is required that the name is
 * valid to represent a "value" of a property in the property-list of MBean ObjectName. The name must be specified and
 * is a primary key for an MBean. An invalid name implies failure of operation.
 */

/* @XmlType(name = "", propOrder = {
    "description",
    "property"
}) */

@Configured
public interface Mbean extends ConfigBeanProxy, Named, PropertyBag {

    /**
     * Gets the value of the objectType property. A String representing whether it is a user-defined MBean or System MBean.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "user")
    public String getObjectType();

    /**
     * Sets the value of the objectType property.
     *
     * @param value allowed object is {@link String }
     */
    public void setObjectType(String value) throws PropertyVetoException;

    /**
     * Gets the value of the implClassName property. A String that represents fully qualified class name of MBean
     * implementation. This is read-only.
     *
     * @return possible object is {@link String }
     */
    @Attribute
    @NotNull
    public String getImplClassName();

    /**
     * Sets the value of the implClassName property.
     *
     * @param value allowed object is {@link String }
     */
    public void setImplClassName(String value) throws PropertyVetoException;

    /**
     * Gets the value of the objectName property.
     *
     * A String that represents a system-generated Object Name for this MBean.
     *
     * @return possible object is {@link String }
     */
    @Attribute
    public String getObjectName();

    /**
     * Sets the value of the objectName property.
     *
     * @param value allowed object is {@link String }
     */
    public void setObjectName(String value) throws PropertyVetoException;

    /**
     * Gets the value of the enabled property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "true", dataType = Boolean.class)
    public String getEnabled();

    /**
     * Sets the value of the enabled property.
     *
     * @param value allowed object is {@link String }
     */
    public void setEnabled(String value) throws PropertyVetoException;

    /**
     * Gets the value of the description property.
     *
     * @return possible object is {@link String }
     */
    @Attribute
    public String getDescription();

    /**
     * Sets the value of the description property.
     *
     * @param value allowed object is {@link String }
     */
    public void setDescription(String value) throws PropertyVetoException;

    /**
     * Properties as per {@link org.jvnet.hk2.config.types.PropertyBag}
     */
    @ToDo(priority = ToDo.Priority.IMPORTANT, details = "Provide PropertyDesc for legal props")
    @PropertiesDesc(props = {})
    @Element
    List<Property> getProperty();
}
