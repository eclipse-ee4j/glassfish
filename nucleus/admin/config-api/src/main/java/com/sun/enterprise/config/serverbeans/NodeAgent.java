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
 * SE/EE Node Controller. The node agent is an agent that manages server instances
 * on a host machine.
 */
@Configured
public interface NodeAgent extends ConfigBeanProxy, PropertyBag {

    /**
     * Gets the value of the {@code name} property.
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
     * Gets the value of the {@code systemJmxConnectorName} property.
     *
     * <p>The name of the internal jmx connector.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getSystemJmxConnectorName();

    /**
     * Sets the value of the {@code systemJmxConnectorName} property.
     *
     * @param connectorName allowed object is {@link String}
     */
    void setSystemJmxConnectorName(String connectorName) throws PropertyVetoException;

    /**
     * Gets the value of the {@code startServersInStartup} property.
     *
     * <p>If {@code true}, starts all managed server instances when
     * the Node Controller is started.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "true", dataType = Boolean.class)
    String getStartServersInStartup();

    /**
     * Sets the value of the {@code startServersInStartup} property.
     *
     * @param startServers allowed object is {@link String}
     */
    void setStartServersInStartup(String startServers) throws PropertyVetoException;

    /**
     * Gets the value of the {@code jmxConnector} property.
     *
     * @return possible object is {@link JmxConnector}
     */
    @Element
    JmxConnector getJmxConnector();

    /**
     * Sets the value of the {@code jmxConnector} property.
     *
     * @param jmxConnector allowed object is {@link JmxConnector}
     */
    void setJmxConnector(JmxConnector jmxConnector) throws PropertyVetoException;

    /**
     * Gets the value of the {@code authRealm} property.
     *
     * @return possible object is {@link AuthRealm}
     */
    @Element
    AuthRealm getAuthRealm();

    /**
     * Sets the value of the {@code authRealm} property.
     *
     * @param authRealm allowed object is {@link AuthRealm}
     */
    void setAuthRealm(AuthRealm authRealm) throws PropertyVetoException;

    /**
     * Gets the value of the {@code logService} property.
     *
     * @return possible object is {@link LogService}
     */
    @Element(required = true)
    LogService getLogService();

    /**
     * Sets the value of the {@code logService} property.
     *
     * @param logService allowed object is {@link LogService}
     */
    void setLogService(LogService logService) throws PropertyVetoException;

    /**
     * Properties as per {@link org.jvnet.hk2.config.types.PropertyBag}
     */
    @ToDo(priority = ToDo.Priority.IMPORTANT, details = "Provide PropertyDesc for legal props")
    @PropertiesDesc(props = {})
    @Element
    List<Property> getProperty();
}
