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
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

import java.beans.PropertyVetoException;
import java.util.List;

import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.quality.ToDo;

import jakarta.validation.constraints.NotNull;

/**
 * SE/EE Node Controller. The node agent is an agent that manages server instances on a host machine.
 */

/* @XmlType(name = "", propOrder = {
    "jmxConnector",
    "authRealm",
    "logService",
    "property"
}) */

@Configured
public interface NodeAgent extends ConfigBeanProxy, PropertyBag {

    /**
     * Gets the value of the name property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(key = true)
    @NotNull
    public String getName();

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is {@link String }
     */
    public void setName(String value) throws PropertyVetoException;

    /**
     * Gets the value of the systemJmxConnectorName property.
     *
     * The name of the internal jmx connector
     *
     * @return possible object is {@link String }
     */
    @Attribute
    public String getSystemJmxConnectorName();

    /**
     * Sets the value of the systemJmxConnectorName property.
     *
     * @param value allowed object is {@link String }
     */
    public void setSystemJmxConnectorName(String value) throws PropertyVetoException;

    /**
     * Gets the value of the startServersInStartup property.
     *
     * If true, starts all managed server instances when the Node Controller is started.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "true", dataType = Boolean.class)
    public String getStartServersInStartup();

    /**
     * Sets the value of the startServersInStartup property.
     *
     * @param value allowed object is {@link String }
     */
    public void setStartServersInStartup(String value) throws PropertyVetoException;

    /**
     * Gets the value of the jmxConnector property.
     *
     * @return possible object is {@link JmxConnector }
     */
    @Element
    public JmxConnector getJmxConnector();

    /**
     * Sets the value of the jmxConnector property.
     *
     * @param value allowed object is {@link JmxConnector }
     */
    public void setJmxConnector(JmxConnector value) throws PropertyVetoException;

    /**
     * Gets the value of the authRealm property.
     *
     * @return possible object is {@link AuthRealm }
     */
    @Element
    public AuthRealm getAuthRealm();

    /**
     * Sets the value of the authRealm property.
     *
     * @param value allowed object is {@link AuthRealm }
     */
    public void setAuthRealm(AuthRealm value) throws PropertyVetoException;

    /**
     * Gets the value of the logService property.
     *
     * @return possible object is {@link LogService }
     */
    @Element(required = true)
    public LogService getLogService();

    /**
     * Sets the value of the logService property.
     *
     * @param value allowed object is {@link LogService }
     */
    public void setLogService(LogService value) throws PropertyVetoException;

    /**
     * Properties as per {@link org.jvnet.hk2.config.types.PropertyBag}
     */
    @ToDo(priority = ToDo.Priority.IMPORTANT, details = "Provide PropertyDesc for legal props")
    @PropertiesDesc(props = {})
    @Element
    List<Property> getProperty();
}
