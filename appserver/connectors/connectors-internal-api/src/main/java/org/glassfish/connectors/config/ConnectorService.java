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

package org.glassfish.connectors.config;

import jakarta.validation.constraints.Min;

import java.beans.PropertyVetoException;
import java.util.List;

import org.glassfish.api.admin.config.ConfigExtension;
import org.glassfish.api.admin.config.PropertiesDesc;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;


/**
 *
 */

/* @XmlType(name = "") */

@Configured
public interface ConnectorService extends ConfigExtension, ConfigBeanProxy, PropertyBag {

    /**
     * Gets the value of the shutdownTimeoutInSeconds property.
         *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="30")
    @Min(value=1)
    public String getShutdownTimeoutInSeconds();

    /**
     * Sets the value of the shutdownTimeoutInSeconds property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setShutdownTimeoutInSeconds(String value) throws PropertyVetoException;

    /**
     * Gets the value of the connector-classloading-policy.<br>
     * Valid values are <i>derived</i> or <i>global</i><br>
     * <i>derived</i> indicates that the resource-adapters are provided according the the
     * references of resource-adapters in application's deployment-descriptors<br>
     * <i>global</i> indicates that all resource-adapters will be visible to all applications.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="derived")
    public String getClassLoadingPolicy();

    /**
     * Sets the value of the connector-classloading-policy.<br>
     * Valid values are <i>derived</i> or <i>global</i><br>
     * <i>derived</i> indicates that the resource-adapters are provided according the the
     * references of resource-adapters in application's deployment-descriptors<br>
     * <i>global</i> indicates that all resource-adapters will be visible to all applications.
     * @param value allowed object is
     *              {@link String }
     */
    public void setClassLoadingPolicy(String value) throws PropertyVetoException;

    /**
     *    Properties as per {@link org.jvnet.hk2.config.types.PropertyBag}
     *
     *  Properties are used to override the ManagedConnectionFactory  javabean
     * configuration settings. When one or more of these properties are
     * specified, they are passed as is using set<Name>(<Value>) methods to the
     * Resource Adapter's ManagedConnectionfactory class (specified in ra.xml).
     *
     */
    @PropertiesDesc(props={})
    @Element
    List<Property> getProperty();

}
