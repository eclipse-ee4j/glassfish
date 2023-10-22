/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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
@Configured
public interface ModuleMonitoringLevels extends ConfigBeanProxy, PropertyBag {

    String MONITORING_LEVELS = "(OFF|LOW|HIGH)";

    String MONITORING_LEVELS_MSG = "Valid values: " + MONITORING_LEVELS;

    /**
     * Gets the value of the {@code threadPool} property.
     *
     * <p>All the {@code thread-pools} used by the run time
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "OFF")
    @Pattern(regexp = MONITORING_LEVELS, message = MONITORING_LEVELS_MSG)
    String getThreadPool();

    /**
     * Sets the value of the {@code threadPool} property.
     *
     * @param threadPool allowed object is {@link String}
     */
    void setThreadPool(String threadPool) throws PropertyVetoException;

    /**
     * Gets the value of the {@code orb} property.
     *
     * <p>Specifies the level for connection managers of the {@code orb}, which apply
     * to connections to the {@code orb}.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "OFF")
    @Pattern(regexp = MONITORING_LEVELS, message = MONITORING_LEVELS_MSG)
    String getOrb();

    /**
     * Sets the value of the {@code orb} property.
     *
     * @param orb allowed object is {@link String}
     */
    void setOrb(String orb) throws PropertyVetoException;

    /**
     * Gets the value of the {@code ejbContainer} property.
     *
     * <p>Various ejbs deployed to the server, ejb-pools, ejb-caches & ejb-methods.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "OFF")
    @Pattern(regexp = MONITORING_LEVELS, message = MONITORING_LEVELS_MSG)
    String getEjbContainer();

    /**
     * Sets the value of the {@code ejbContainer} property.
     *
     * @param ejbContainer allowed object is {@link String}
     */
    void setEjbContainer(String ejbContainer) throws PropertyVetoException;

    /**
     * Gets the value of the {@code webContainer} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "OFF")
    @Pattern(regexp = MONITORING_LEVELS, message = MONITORING_LEVELS_MSG)
    String getWebContainer();

    /**
     * Sets the value of the {@code webContainer} property.
     *
     * @param webContainer allowed object is {@link String}
     */
    void setWebContainer(String webContainer) throws PropertyVetoException;

    /**
     * Gets the value of the {@code deployment} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "OFF")
    @Pattern(regexp = MONITORING_LEVELS, message = MONITORING_LEVELS_MSG)
    String getDeployment();

    /**
     * Sets the value of the {@code webContainer} property.
     *
     * @param deployment allowed object is {@link String}
     */
    void setDeployment(String deployment) throws PropertyVetoException;

    /**
     * Gets the value of the {@code transactionService} property.
     *
     * <p>Transaction subsystem.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "OFF")
    @Pattern(regexp = MONITORING_LEVELS, message = MONITORING_LEVELS_MSG)
    String getTransactionService();

    /**
     * Sets the value of the {@code transactionService} property.
     *
     * @param transactionService allowed object is {@link String}
     */
    void setTransactionService(String transactionService) throws PropertyVetoException;

    /**
     * Gets the value of the {@code httpService} property.
     *
     * <p>HTTP engine and the HTTP listeners therein.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "OFF")
    @Pattern(regexp = MONITORING_LEVELS, message = MONITORING_LEVELS_MSG)
    String getHttpService();

    /**
     * Sets the value of the {@code httpService} property.
     *
     * @param httpService allowed object is {@link String}
     */
    void setHttpService(String httpService) throws PropertyVetoException;

    /**
     * Gets the value of the {@code jdbcConnectionPool} property.
     *
     * <p>Monitoring level for all the {@code jdbc-connection-pools} used by the runtime.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "OFF")
    @Pattern(regexp = MONITORING_LEVELS, message = MONITORING_LEVELS_MSG)
    String getJdbcConnectionPool();

    /**
     * Sets the value of the {@code jdbcConnectionPool} property.
     *
     * @param jdbcConnectionPool allowed object is {@link String}
     */
    void setJdbcConnectionPool(String jdbcConnectionPool) throws PropertyVetoException;

    /**
     * Gets the value of the {@code connectorConnectionPool} property.
     *
     * <p>Monitoring level for all the {@code connector-connection-pools} used by runtime.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "OFF")
    @Pattern(regexp = MONITORING_LEVELS, message = MONITORING_LEVELS_MSG)
    String getConnectorConnectionPool();

    /**
     * Sets the value of the {@code connectorConnectionPool} property.
     *
     * @param connectorConnectionPool allowed object is {@link String}
     */
    void setConnectorConnectionPool(String connectorConnectionPool) throws PropertyVetoException;

    /**
     * Gets the value of the {@code connectorService} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "OFF")
    @Pattern(regexp = MONITORING_LEVELS, message = MONITORING_LEVELS_MSG)
    String getConnectorService();

    /**
     * Sets the value of the {@code connectorService} property.
     *
     * @param connectorService allowed object is {@link String}
     */
    void setConnectorService(String connectorService) throws PropertyVetoException;

    /**
     * Gets the value of the {@code jmsService} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "OFF")
    @Pattern(regexp = MONITORING_LEVELS, message = MONITORING_LEVELS_MSG)
    String getJmsService();

    /**
     * Sets the value of the {@code jmsService} property.
     *
     * @param jmsService allowed object is {@link String}
     */
    void setJmsService(String jmsService) throws PropertyVetoException;

    /**
     * Gets the value of the {@code jvm} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "OFF")
    @Pattern(regexp = MONITORING_LEVELS, message = MONITORING_LEVELS_MSG)
    String getJvm();

    /**
     * Sets the value of the {@code jvm} property.
     *
     * @param jvm allowed object is {@link String}
     */
    void setJvm(String jvm) throws PropertyVetoException;

    /**
     * Gets the value of the {@code security} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "OFF")
    @Pattern(regexp = MONITORING_LEVELS, message = MONITORING_LEVELS_MSG)
    String getSecurity();

    /**
     * Sets the value of the {@code security} property.
     *
     * @param security allowed object is {@link String}
     */
    void setSecurity(String security) throws PropertyVetoException;

    /**
     * Gets the value of the {@code webServiceContainer} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "OFF")
    @Pattern(regexp = MONITORING_LEVELS, message = MONITORING_LEVELS_MSG)
    String getWebServicesContainer();

    /**
     * Sets the value of the {@code webServiceContainer} property.
     *
     * @param webServicesContainer allowed object is {@link String}
     */
    void setWebServicesContainer(String webServicesContainer) throws PropertyVetoException;

    /**
     * Gets the value of the {@code jpa} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "OFF")
    @Pattern(regexp = MONITORING_LEVELS, message = MONITORING_LEVELS_MSG)
    String getJpa();

    /**
     * Sets the value of the {@code jpa} property.
     *
     * @param jpa allowed object is {@link String}
     */
    void setJpa(String jpa) throws PropertyVetoException;

    /**
     * Gets the value of the {@code jax-rs} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "OFF")
    @Pattern(regexp = MONITORING_LEVELS, message = MONITORING_LEVELS_MSG)
    String getJersey();

    /**
     * Sets the value of the {@code jax-rs} property.
     *
     * @param jersey allowed object is {@link String}
     */
    void setJersey(String jersey) throws PropertyVetoException;

    /**
     * Gets the value of the {@code cloudTenantManager} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "OFF")
    @Pattern(regexp = MONITORING_LEVELS, message = MONITORING_LEVELS_MSG)
    String getCloudTenantManager();

    /**
     * Sets the value of the {@code cloudTenantManager} property.
     *
     * @param cloudTenantManager allowed object is {@link String}
     */
    void setCloudTenantManager(String cloudTenantManager) throws PropertyVetoException;

    /**
     * Gets the value of the {@code cloud} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "OFF")
    @Pattern(regexp = MONITORING_LEVELS, message = MONITORING_LEVELS_MSG)
    String getCloud();

    /**
     * Sets the value of the {@code cloud} property.
     *
     * @param cloud allowed object is {@link String}
     */
    void setCloud(String cloud) throws PropertyVetoException;

    /**
     * Gets the value of the {@code cloudOrchestrator} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "OFF")
    @Pattern(regexp = MONITORING_LEVELS, message = MONITORING_LEVELS_MSG)
    String getCloudOrchestrator();

    /**
     * Sets the value of the {@code cloudOrchestrator} property.
     *
     * @param cloudOrchestrator allowed object is {@link String}
     */
    void setCloudOrchestrator(String cloudOrchestrator) throws PropertyVetoException;

    /**
     * Gets the value of the {@code cloudElasticity} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "OFF")
    @Pattern(regexp = MONITORING_LEVELS, message = MONITORING_LEVELS_MSG)
    String getCloudElasticity();

    /**
     * Sets the value of the {@code cloudElasticity} property.
     *
     * @param cloudElasticity allowed object is {@link String}
     */
    void setCloudElasticity(String cloudElasticity) throws PropertyVetoException;

    /**
     * Gets the value of the cloud IMS property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "OFF")
    @Pattern(regexp = MONITORING_LEVELS, message = MONITORING_LEVELS_MSG)
    String getCloudVirtAssemblyService();

    /**
     * Sets the value of the cloud IMS property.
     *
     * @param virtAssemblyService allowed object is {@link String}
     */
    void setCloudVirtAssemblyService(String virtAssemblyService) throws PropertyVetoException;

    /**
     * Properties as per {@link PropertyBag}
     */
    @Override
    @ToDo(priority = ToDo.Priority.IMPORTANT, details = "Provide PropertyDesc for legal props")
    @PropertiesDesc(props = {})
    @Element
    List<Property> getProperty();
}
