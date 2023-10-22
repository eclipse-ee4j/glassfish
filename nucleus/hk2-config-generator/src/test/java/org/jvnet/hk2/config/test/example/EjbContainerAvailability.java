/*
 * Copyright (c) 2021, 2023 Contributors to the Eclipse Foundation
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

package org.jvnet.hk2.config.test.example;

import java.beans.PropertyVetoException;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;


/* @XmlType(name = "", propOrder = {
    "property"
}) */

@Configured
public interface EjbContainerAvailability extends ConfigBeanProxy {

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

}
