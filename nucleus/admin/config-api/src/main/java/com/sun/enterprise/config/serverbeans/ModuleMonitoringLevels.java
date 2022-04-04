/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import jakarta.validation.constraints.Pattern;

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
 *
 */

/* @XmlType(name = "", propOrder = {
    "property"
}) */

@Configured
public interface ModuleMonitoringLevels extends ConfigBeanProxy, PropertyBag {

    String MONITORING_LEVELS = "(OFF|LOW|HIGH)";
    String MONITORING_LEVELS_MSG = "Valid values: " + MONITORING_LEVELS;

    /**
     * Gets the value of the threadPool property.
     * All the thread-pools used by the run time
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "OFF")
    @Pattern(regexp = MONITORING_LEVELS, message = MONITORING_LEVELS_MSG)
    String getThreadPool();

    /**
     * Sets the value of the threadPool property.
     *
     * @param value allowed object is {@link String }
     */
    void setThreadPool(String value) throws PropertyVetoException;

    /**
     * Gets the value of the orb property.
     * Specifies the level for connection managers of the orb, which apply to connections to the orb
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "OFF")
    @Pattern(regexp = MONITORING_LEVELS, message = MONITORING_LEVELS_MSG)
    String getOrb();

    /**
     * Sets the value of the orb property.
     *
     * @param value allowed object is {@link String }
     */
    void setOrb(String value) throws PropertyVetoException;

    /**
     * Gets the value of the ejbContainer property.
     * Various ejbs deployed to the server, ejb-pools, ejb-caches & ejb-methods
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "OFF")
    @Pattern(regexp = MONITORING_LEVELS, message = MONITORING_LEVELS_MSG)
    String getEjbContainer();

    /**
     * Sets the value of the ejbContainer property.
     *
     * @param value allowed object is {@link String }
     */
    void setEjbContainer(String value) throws PropertyVetoException;

    /**
     * Gets the value of the webContainer property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "OFF")
    @Pattern(regexp = MONITORING_LEVELS, message = MONITORING_LEVELS_MSG)
    String getWebContainer();

    /**
     * Sets the value of the webContainer property.
     *
     * @param value allowed object is {@link String }
     */
    void setWebContainer(String value) throws PropertyVetoException;

    /**
     * Gets the value of the deployment property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "OFF")
    @Pattern(regexp = MONITORING_LEVELS, message = MONITORING_LEVELS_MSG)
    String getDeployment();

    /**
     * Sets the value of the webContainer property.
     *
     * @param value allowed object is {@link String }
     */
    void setDeployment(String value) throws PropertyVetoException;

    /**
     * Gets the value of the transactionService property.
     * Transaction subsystem
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "OFF")
    @Pattern(regexp = MONITORING_LEVELS, message = MONITORING_LEVELS_MSG)
    String getTransactionService();

    /**
     * Sets the value of the transactionService property.
     *
     * @param value allowed object is {@link String }
     */
    void setTransactionService(String value) throws PropertyVetoException;

    /**
     * Gets the value of the httpService property.
     * http engine and the http listeners therein.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "OFF")
    @Pattern(regexp = MONITORING_LEVELS, message = MONITORING_LEVELS_MSG)
    String getHttpService();

    /**
     * Sets the value of the httpService property.
     *
     * @param value allowed object is {@link String }
     */
    void setHttpService(String value) throws PropertyVetoException;

    /**
     * Gets the value of the jdbcConnectionPool property.
     * Monitoring level for all the jdbc-connection-pools used by the runtime.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "OFF")
    @Pattern(regexp = MONITORING_LEVELS, message = MONITORING_LEVELS_MSG)
    String getJdbcConnectionPool();

    /**
     * Sets the value of the jdbcConnectionPool property.
     *
     * @param value allowed object is {@link String }
     */
    void setJdbcConnectionPool(String value) throws PropertyVetoException;

    /**
     * Gets the value of the connectorConnectionPool property.
     * Monitoring level for all the connector-connection-pools used by runtime.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "OFF")
    @Pattern(regexp = MONITORING_LEVELS, message = MONITORING_LEVELS_MSG)
    String getConnectorConnectionPool();

    /**
     * Sets the value of the connectorConnectionPool property.
     *
     * @param value allowed object is {@link String }
     */
    void setConnectorConnectionPool(String value) throws PropertyVetoException;

    /**
     * Gets the value of the connectorService property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "OFF")
    @Pattern(regexp = MONITORING_LEVELS, message = MONITORING_LEVELS_MSG)
    String getConnectorService();

    /**
     * Sets the value of the connectorService property.
     *
     * @param value allowed object is {@link String }
     */
    void setConnectorService(String value) throws PropertyVetoException;

    /**
     * Gets the value of the jmsService property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "OFF")
    @Pattern(regexp = MONITORING_LEVELS, message = MONITORING_LEVELS_MSG)
    String getJmsService();

    /**
     * Sets the value of the jmsService property.
     *
     * @param value allowed object is {@link String }
     */
    void setJmsService(String value) throws PropertyVetoException;

    /**
     * Gets the value of the jvm property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "OFF")
    @Pattern(regexp = MONITORING_LEVELS, message = MONITORING_LEVELS_MSG)
    String getJvm();

    /**
     * Sets the value of the jvm property.
     *
     * @param value allowed object is {@link String }
     */
    void setJvm(String value) throws PropertyVetoException;

    /**
     * Gets the value of the security property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "OFF")
    @Pattern(regexp = MONITORING_LEVELS, message = MONITORING_LEVELS_MSG)
    String getSecurity();

    /**
     * Sets the value of the security property.
     *
     * @param value allowed object is {@link String }
     */
    void setSecurity(String value) throws PropertyVetoException;

    /**
     * Gets the value of the web-service-container property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "OFF")
    @Pattern(regexp = MONITORING_LEVELS, message = MONITORING_LEVELS_MSG)
    String getWebServicesContainer();

    /**
     * Sets the value of the web-service-container property.
     *
     * @param value allowed object is {@link String }
     */
    void setWebServicesContainer(String value) throws PropertyVetoException;

    /**
     * Gets the value of the jpa property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "OFF")
    @Pattern(regexp = MONITORING_LEVELS, message = MONITORING_LEVELS_MSG)
    String getJpa();

    /**
     * Sets the value of the jpa property.
     *
     * @param value allowed object is {@link String }
     */
    void setJpa(String value) throws PropertyVetoException;

    /**
     * Gets the value of the jax-ra property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "OFF")
    @Pattern(regexp = MONITORING_LEVELS, message = MONITORING_LEVELS_MSG)
    String getJersey();

    /**
     * Sets the value of the jax-ra property.
     *
     * @param value allowed object is {@link String }
     */
    void setJersey(String value) throws PropertyVetoException;

    /**
     * Gets the value of the cloudTenantManager property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "OFF")
    @Pattern(regexp = MONITORING_LEVELS, message = MONITORING_LEVELS_MSG)
    String getCloudTenantManager();

    /**
     * Sets the value of the cloudTenantManager property.
     *
     * @param value allowed object is {@link String }
     */
    void setCloudTenantManager(String value) throws PropertyVetoException;

    /**
     * Gets the value of the cloud property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "OFF")
    @Pattern(regexp = MONITORING_LEVELS, message = MONITORING_LEVELS_MSG)
    String getCloud();

    /**
     * Sets the value of the cloud property.
     *
     * @param value allowed object is {@link String }
     */
    void setCloud(String value) throws PropertyVetoException;

    /**
     * Gets the value of the cloud Orchestrator property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "OFF")
    @Pattern(regexp = MONITORING_LEVELS, message = MONITORING_LEVELS_MSG)
    String getCloudOrchestrator();

    /**
     * Sets the value of the cloud Orchestrator property.
     *
     * @param value allowed object is {@link String }
     */
    void setCloudOrchestrator(String value) throws PropertyVetoException;

    /**
     * Gets the value of the cloud Elasticity property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "OFF")
    @Pattern(regexp = MONITORING_LEVELS, message = MONITORING_LEVELS_MSG)
    String getCloudElasticity();

    /**
     * Sets the value of the cloud elasticity property.
     *
     * @param value allowed object is {@link String }
     */
    void setCloudElasticity(String value) throws PropertyVetoException;

    /**
     * Gets the value of the cloud IMS property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "OFF")
    @Pattern(regexp = MONITORING_LEVELS, message = MONITORING_LEVELS_MSG)
    String getCloudVirtAssemblyService();

    /**
     * Sets the value of the cloud IMS property.
     *
     * @param value allowed object is {@link String }
     */
    void setCloudVirtAssemblyService(String value) throws PropertyVetoException;

    /**
     * Properties as per {@link PropertyBag}
     */
    @Override
    @ToDo(priority = ToDo.Priority.IMPORTANT, details = "Provide PropertyDesc for legal props")
    @PropertiesDesc(props = {})
    @Element
    List<Property> getProperty();
}
