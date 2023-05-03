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

/**
 * web-container-availability SE/EE only
 */

/* @XmlType(name = "", propOrder = {
    "property"
}) */

@Configured
public interface WebContainerAvailability extends ConfigBeanProxy {

    /**
     * Gets the value of the availabilityEnabled property.
     *
     * This boolean flag controls whether availability is enabled for HTTP
     * session persistence. If this is "false", then session persistence is
     * disabled for all web modules in j2ee apps and stand-alone web modules.
     * If it is "true" (and providing that the global availability-enabled in
     * availability-service is also "true", then j2ee apps and stand-alone web
     * modules may be ha enabled. Finer-grained control exists at lower levels.
     * If this attribute is missing, it "inherits" the value of the global
     * availability-enabled under availability-service.  Default is "true".
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
     * Gets the value of the persistenceType property.
     *
     * Specifies the session persistence mechanism for web applications that
     * have availability enabled. Default is "replicated".
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute(defaultValue="replicated")
    String getPersistenceType();

    /**
     * Sets the value of the persistenceType property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setPersistenceType(String value) throws PropertyVetoException;

    /**
     * Gets the value of the persistenceFrequency property.
     *
     * The persistence frequency used by the session persistence framework,
     * when persistence-type = "ha". Values may be "time-based" or "web-event"
     * If it is missing, then the persistence-type will revert to "memory".
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute(defaultValue="web-method")
    String getPersistenceFrequency();

    /**
     * Sets the value of the persistenceFrequency property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setPersistenceFrequency(String value) throws PropertyVetoException;

    /**
     * Gets the value of the persistenceScope property.
     *
     * The persistence scope used by the session persistence framework, when
     * persistence-type = "ha". Values may be "session", "modified-session",
     * "modified-attribute". If it is missing, then the persistence-type will
     * revert to "memory".
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute(defaultValue="session")
    String getPersistenceScope();

    /**
     * Sets the value of the persistenceScope property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setPersistenceScope(String value) throws PropertyVetoException;

    /**
     * Gets the value of the persistenceStoreHealthCheckEnabled property.
     *
     * Deprecated. This attribute has no effect. If you wish to control
     * enabling/disabling HADB health check, refer to store-healthcheck-enabled
     * attribute in the availability-service element.
     *
     * @return possible object is
     *         {@link String }
     */
    @Deprecated
    @Attribute(defaultValue = "false", dataType = Boolean.class)
    String getPersistenceStoreHealthCheckEnabled();

    /**
     * Sets the value of the persistenceStoreHealthCheckEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setPersistenceStoreHealthCheckEnabled(String value) throws PropertyVetoException;

    /**
     * Gets the value of the ssoFailoverEnabled property.
     *
     * Controls whether Single-Sign-On state will be made available for failover
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute(defaultValue = "false", dataType = Boolean.class)
    String getSsoFailoverEnabled();

    /**
     * Sets the value of the ssoFailoverEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setSsoFailoverEnabled(String value) throws PropertyVetoException;

    /**
     * Gets the value of the httpSessionStorePoolName property.
     * This is the jndi-name for the JDBC Connection Pool used by the HTTP
     * Session Persistence Framework. If missing, internal code will default it
     * to value of store-pool-name under availability-service
     * (ultimately "jdbc/hastore").
     *
     * @return possible object is
     *         {@link String }
     */
    @Deprecated
    @Attribute
    String getHttpSessionStorePoolName();

    /**
     * Sets the value of the httpSessionStorePoolName property.
     *
     * @param value allowed object is
     *            {@link String }
     */
    void setHttpSessionStorePoolName(String value) throws PropertyVetoException;

    /**
     * Gets thevalue of disableJreplica property.
     * This is the property used to disable setting the JREPLICA cookie
     *
     * @return returns the string representation of the boolean value
     */
    @Attribute(defaultValue = "false", dataType = Boolean.class)
    String getDisableJreplica();

    /**
     * Sets the disableJreplica property
     *
     * @param value allowed object is {@link String}
     * @throws PropertyVetoException
     */
    void setDisableJreplica(String value) throws PropertyVetoException;

}
