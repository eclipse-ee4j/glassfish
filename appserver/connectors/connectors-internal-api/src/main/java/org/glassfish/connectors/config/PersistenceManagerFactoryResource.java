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

package org.glassfish.connectors.config;

import com.sun.enterprise.config.serverbeans.BindableResource;
import com.sun.enterprise.config.serverbeans.Resource;
import org.glassfish.resourcebase.resources.ResourceDeploymentOrder;
import org.glassfish.resourcebase.resources.ResourceTypeOrder;
import org.jvnet.hk2.config.*;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

import java.beans.PropertyVetoException;
import java.util.List;

import org.glassfish.api.admin.config.PropertiesDesc;

import org.glassfish.quality.ToDo;


/**
 * Persistence Manager runtime configuration
 */

/* @XmlType(name = "", propOrder = {
    "description",
    "property"
}) */
@Configured
@ResourceTypeOrder(deploymentOrder= ResourceDeploymentOrder.PERSISTENCE_RESOURCE)
public interface PersistenceManagerFactoryResource extends ConfigBeanProxy, Resource, PropertyBag, BindableResource {

    /**
     * Gets the value of the factoryClass property.
     *
     * Class that creates persistence manager instance
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="com.sun.jdo.spi.persistence.support.sqlstore.impl.PersistenceManagerFactoryImpl")
    public String getFactoryClass();

    /**
     * Sets the value of the factoryClass property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setFactoryClass(String value) throws PropertyVetoException;

    /**
     * Gets the value of the jdbcResourceJndiName property.
     *
     * jdbc resource with which database connections are obtained
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getJdbcResourceJndiName();

    /**
     * Sets the value of the jdbcResourceJndiName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setJdbcResourceJndiName(String value) throws PropertyVetoException;

    /**
     * Gets the value of the enabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="true",dataType=Boolean.class)
    public String getEnabled();

    /**
     * Sets the value of the enabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEnabled(String value) throws PropertyVetoException;

    /**
     * Gets the value of the description property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getDescription();

    /**
     * Sets the value of the description property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDescription(String value) throws PropertyVetoException;

    /**
        Properties as per {@link org.glassfish.api.admin.config.PropertyBag}
     */
    @ToDo(priority=ToDo.Priority.IMPORTANT, details="Provide PropertyDesc for legal props" )
    @PropertiesDesc(props={})
    @Element
    List<Property> getProperty();

    @DuckTyped
    String getIdentity();

    class Duck {
        public static String getIdentity(PersistenceManagerFactoryResource resource){
            return resource.getJndiName();
        }
    }

}
