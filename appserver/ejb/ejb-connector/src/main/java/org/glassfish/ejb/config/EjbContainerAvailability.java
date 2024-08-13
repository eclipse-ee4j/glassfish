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

package org.glassfish.ejb.config;

import com.sun.enterprise.config.serverbeans.AvailabilityServiceExtension;

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


/* @XmlType(name = "", propOrder = {
    "property"
}) */

@Configured
public interface EjbContainerAvailability extends ConfigBeanProxy,
        PropertyBag, AvailabilityServiceExtension {

    /**
     * Gets the value of the availabilityEnabled property.
     *
     * This boolean flag controls whether availability is enabled for SFSB
     * checkpointing (and potentially passivation). If this is "false",
     * then all SFSB checkpointing is disabled for all j2ee apps and ejb modules
     * If it is "true" (and providing that the global availability-enabled in
     * availability-service is also "true", then j2ee apps and stand-alone ejb
     * modules may be ha enabled. Finer-grained control exists at lower levels.
     * If this attribute is missing, it inherits the value of the global
     * availability-enabled under availability-service.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute(defaultValue="true")
    String getAvailabilityEnabled();

    /**
     * Sets the value of the availabilityEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setAvailabilityEnabled(String value) throws PropertyVetoException;

    /**
     * Gets the value of the sfsbHaPersistenceType property.
     *
     * The persistence type used by the EJB Stateful Session Bean Container for
     * checkpointing and passivating availability-enabled beans' state.
     * Default is "ha".
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="replicated")
    String getSfsbHaPersistenceType();

    /**
     * Sets the value of the sfsbHaPersistenceType property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setSfsbHaPersistenceType(String value) throws PropertyVetoException;

    /**
     * Gets the value of the sfsbPersistenceType property.
     *
     * Specifies the passivation mechanism for stateful session beans that do
     * not have availability enabled. Default is "file".
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="file")
    String getSfsbPersistenceType();

    /**
     * Sets the value of the sfsbPersistenceType property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setSfsbPersistenceType(String value) throws PropertyVetoException;

    /**
     * Gets the value of the sfsbCheckpointEnabled property.
     *
     * This attribute is deprecated, replaced by availability-enabled and will
     * be ignored if present.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    String getSfsbCheckpointEnabled();

    /**
     * Sets the value of the sfsbCheckpointEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setSfsbCheckpointEnabled(String value) throws PropertyVetoException;

    /**
     * Gets the value of the sfsbQuickCheckpointEnabled property.
     *
     * This attribute is deprecated and will be ignored if present.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    String getSfsbQuickCheckpointEnabled();

    /**
     * Sets the value of the sfsbQuickCheckpointEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setSfsbQuickCheckpointEnabled(String value) throws PropertyVetoException;

    /**
     * Gets the value of the sfsbStorePoolName property.
     * This is the jndi-name for the JDBC Connection Pool used by the
     * EJB Stateful Session Bean Container for use in checkpointing/passivation
     * when persistence-type = "ha". See sfsb-ha-persistence-type and
     * sfsb-persistence-type for more details. It will default to value of
     * store-pool-name under availability-service (ultimately "jdbc/hastore").
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    String getSfsbStorePoolName();

    /**
     * Sets the value of the sfsbStorePoolName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setSfsbStorePoolName(String value) throws PropertyVetoException;

    /**
        Properties as per {@link PropertyBag}
     */
    @ToDo(priority=ToDo.Priority.IMPORTANT, details="Provide PropertyDesc for legal props" )
    @PropertiesDesc(props={})
    @Element
    List<Property> getProperty();
}
