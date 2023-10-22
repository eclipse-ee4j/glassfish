/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package org.glassfish.connectors.config;

import com.sun.enterprise.config.serverbeans.BindableResource;
import com.sun.enterprise.config.serverbeans.Resource;

import java.beans.PropertyVetoException;
import java.util.List;

import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.quality.ToDo;
import org.glassfish.resourcebase.resources.ResourceDeploymentOrder;
import org.glassfish.resourcebase.resources.ResourceTypeOrder;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

/**
 * Persistence Manager runtime configuration.
 */
@Configured
@ResourceTypeOrder(deploymentOrder = ResourceDeploymentOrder.PERSISTENCE_RESOURCE)
public interface PersistenceManagerFactoryResource extends ConfigBeanProxy, Resource, PropertyBag, BindableResource {

    /**
     * Gets the value of the {@code factoryClass} property.
     *
     * <p>Class that creates persistence manager instance.
     *
     * @return possible object is {@link String}
     */
    @Attribute (defaultValue = "com.sun.jdo.spi.persistence.support.sqlstore.impl.PersistenceManagerFactoryImpl")
    String getFactoryClass();

    /**
     * Sets the value of the {@code factoryClass} property.
     *
     * @param factoryClass allowed object is {@link String}
     */
    void setFactoryClass(String factoryClass) throws PropertyVetoException;

    /**
     * Gets the value of the {@code jdbcResourceJndiName} property.
     *
     * <p>JDBC resource with which database connections are obtained.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getJdbcResourceJndiName();

    /**
     * Sets the value of the {@code jdbcResourceJndiName} property.
     *
     * @param jndiName allowed object is {@link String}
     */
    void setJdbcResourceJndiName(String jndiName) throws PropertyVetoException;

    /**
     * Gets the value of the {@code enabled} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute (defaultValue = "true",dataType = Boolean.class)
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
        Properties as per {@link PropertyBag}.
     */
    @ToDo(priority = ToDo.Priority.IMPORTANT, details = "Provide PropertyDesc for legal props" )
    @PropertiesDesc(props = {})
    @Element
    List<Property> getProperty();

    default String getIdentity() {
        return getJndiName();
    }
}
