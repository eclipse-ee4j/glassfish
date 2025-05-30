/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.connectors;

import com.sun.enterprise.connectors.util.ConnectionPoolReconfigHelper.ReconfigAction;
import com.sun.enterprise.deployment.ConnectorConfigProperty;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This class abstract the ra.xml values pertaining to the connection
 * management. It contains various config properties of MCF, Resource adapter,
 * Connection and also their respective classes and interfaces.
 *
 * @author Srikanth P
 */
public final class ConnectorDescriptorInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    private String rarName;
    private String resourceAdapterClass;
    private String connectionDefinitionName;
    private String managedConnectionFactoryClass;
    private String connectionFactoryClass;
    private String connectionFactoryInterface;
    private String connectionClass;
    private String connectionInterface;
    private Set<ConnectorConfigProperty> mcfConfigProperties;
    private Set<ConnectorConfigProperty > resourceAdapterConfigProperties;

    /**
     * Default constructor
     */
    public ConnectorDescriptorInfo() {
        this.mcfConfigProperties = new LinkedHashSet< >();
        this.resourceAdapterConfigProperties = new LinkedHashSet< >();
    }

    /**
     * Clone method
     *
     * @return ConnectorDescriptorInfo instance
     */
    public ConnectorDescriptorInfo doClone() {
        ConnectorDescriptorInfo cdi = new ConnectorDescriptorInfo();
        cdi.setMCFConfigProperties(mcfConfigProperties);
        cdi.setResourceAdapterConfigProperties(resourceAdapterConfigProperties);
        cdi.setRarName(rarName);
        cdi.setResourceAdapterClassName(resourceAdapterClass);
        cdi.setConnectionDefinitionName(connectionDefinitionName);
        cdi.setManagedConnectionFactoryClass(managedConnectionFactoryClass);
        cdi.setConnectionFactoryClass(connectionFactoryClass);
        cdi.setConnectionFactoryInterface(connectionFactoryInterface);
        cdi.setConnectionClass(connectionClass);
        cdi.setConnectionInterface(connectionInterface);
        return cdi;
    }

    /**
     * Constructor
     *
     * @param mcfConfigProperties Array of MCF config properties
     * @param resourceAdapterConfigProperties
     *                            Array of  Resource adapter config props
     */
    public ConnectorDescriptorInfo(ConnectorConfigProperty[] mcfConfigProperties,
        ConnectorConfigProperty[] resourceAdapterConfigProperties) {
        this();
        if (mcfConfigProperties != null) {
            for (ConnectorConfigProperty mcfConfigProperty : mcfConfigProperties) {
                this.mcfConfigProperties.add(mcfConfigProperty);
            }

            if (resourceAdapterConfigProperties != null) {
                for (ConnectorConfigProperty mcfConfigProperty : mcfConfigProperties) {
                    this.resourceAdapterConfigProperties.add(mcfConfigProperty);
                }
            }
        }
    }

    /**
     * Adds an MCF config property to the existing array/Set of MCF config
     * properties.
     *
     * @param configProperty Config property to be added.
     */
    public void addMCFConfigProperty(ConnectorConfigProperty configProperty) {
        if (configProperty != null) {
            mcfConfigProperties.add(configProperty);
        }
    }

    /**
     * Removes an config property from the existing array/Set of MCF config
     * properties
     *
     * @param configProperty Config property to be removed.
     */
    public void removeMCFConfigProperty(ConnectorConfigProperty configProperty) {
        if (configProperty != null) {
            mcfConfigProperties.remove(configProperty);
        }
    }

    /**
     * Setter method for MCFConfigProperties property.
     *
     * @param configProperties Set MCF config properties
     */
    public void setMCFConfigProperties(Set<ConnectorConfigProperty> configProperties) {
        mcfConfigProperties = configProperties;
    }

    /**
     * Setter method for MCFConfigProperties property.
     *
     * @param configProperties Array of MCF config properties
     */
    public void setMCFConfigProperties(ConnectorConfigProperty[] configProperties) {
        if (configProperties != null) {
            for (ConnectorConfigProperty element : configProperties) {
                mcfConfigProperties.add(element);
            }
        }
    }

    /**
     * Getter method for MCFConfigProperties property
     *
     * @return Set of managed connection factory config properties
     */
    public Set<ConnectorConfigProperty> getMCFConfigProperties() {
        return mcfConfigProperties;
    }

    /**
     * Adds a Resource Adapter config property to the existing array/Set
     * of Resource Adapter config properties.
     *
     * @param configProperty Config property to be added.
     */
    public void addResourceAdapterConfigProperty(ConnectorConfigProperty  configProperty) {
        if (configProperty != null) {
            resourceAdapterConfigProperties.add(configProperty);
        }
    }

    /**
     * Removes a Resource Adapter config property to the existing array/Set
     * of Resource Adapter config properties.
     *
     * @param configProperty Config property to be removed.
     */
    public void removeResourceAdapterConfigProperty(ConnectorConfigProperty configProperty) {
        if (configProperty != null) {
            resourceAdapterConfigProperties.remove(configProperty);
        }
    }

    /**
     * Setter method for ResourceAdapterConfigProperties property.
     *
     * @param configProperties Set ResourceAdapter config properties
     */
    public void setResourceAdapterConfigProperties(Set<ConnectorConfigProperty> configProperties) {
        resourceAdapterConfigProperties = configProperties;
    }

    /**
     * Setter method for ResourceAdapterConfigProperties property.
     *
     * @param configProperties Array ResourceAdapter config properties
     */
    public void setResourceAdapterConfigProperties(ConnectorConfigProperty[] configProperties) {
        if (configProperties != null) {
            for (ConnectorConfigProperty configProperty : configProperties) {
                resourceAdapterConfigProperties.add(configProperty);
            }
        }
    }

    /**
     * Getter method for ResourceAdapterConfigProperties property
     *
     * @return Set of resource adapter config properties
     */
    public Set<ConnectorConfigProperty> getResourceAdapterConfigProperties() {
        return resourceAdapterConfigProperties;
    }

    /**
     * Getter method for RarName property
     *
     * @return rarName
     */
    public String getRarName() {
        return rarName;
    }

    /**
     * Setter method for RarName property
     *
     * @param rarName rar name
     */
    public void setRarName(String rarName) {
        this.rarName = rarName;
    }

    /**
     * Getter method for ResourceAdapterClassName property
     *
     * @return Resource adapter class name
     */
    public String getResourceAdapterClassName() {
        return resourceAdapterClass;
    }

    /**
     * Setter method for ResourceAdapterClassName property
     *
     * @param resourceAdapterClass Resource adapter class name
     */
    public void setResourceAdapterClassName(String resourceAdapterClass) {
        this.resourceAdapterClass = resourceAdapterClass;
    }

    /**
     * Getter method for ConnectionDefinitionName property
     *
     * @return connection definition name
     */
    public String getConnectionDefinitionName() {
        return connectionDefinitionName;
    }

    /**
     * Setter method for ConnectionDefinitionName property
     *
     * @param connectionDefinitionName connection definition name
     */
    public void setConnectionDefinitionName(String connectionDefinitionName) {
        this.connectionDefinitionName = connectionDefinitionName;
    }

    /**
     * Getter method for ManagedConnectionFactoryClass property
     *
     * @return managed connection factory class
     */
    public String getManagedConnectionFactoryClass() {
        return managedConnectionFactoryClass;
    }

    /**
     * Setter method for ManagedConnectionFactoryClass property
     *
     * @param managedConnectionFactoryClass managed connection factory class
     */
    public void setManagedConnectionFactoryClass(String managedConnectionFactoryClass) {
        this.managedConnectionFactoryClass = managedConnectionFactoryClass;
    }

    /**
     * Getter method for ConnectionFactoryClass property
     *
     * @return connection factory class
     */
    public String getConnectionFactoryClass() {
        return connectionFactoryClass;
    }

    /**
     * Setter method for ConnectionFactoryClass property
     *
     * @param connectionFactoryClass connection factory class
     */
    public void setConnectionFactoryClass(String connectionFactoryClass) {
        this.connectionFactoryClass = connectionFactoryClass;
    }

    /**
     * Getter method for ConnectionFactoryInterface property
     *
     * @return connection factory interface class
     */
    public String getConnectionFactoryInterface() {
        return connectionFactoryInterface;
    }

    /**
     * Setter method for ConnectionFactoryInterface property
     *
     * @param connectionFactoryInterface connection factory interface class
     */
    public void setConnectionFactoryInterface(String connectionFactoryInterface) {
        this.connectionFactoryInterface = connectionFactoryInterface;
    }

    /**
     * Getter method for ConnectionClass property
     *
     * @return connection class
     */
    public String getConnectionClass() {
        return connectionClass;
    }

    /**
     * Setter method for ConnectionClass property
     *
     * @param connectionClass connection Class
     */
    public void setConnectionClass(String connectionClass) {
        this.connectionClass = connectionClass;
    }

    /**
     * Getter method for ConnectionInterface property
     *
     * @return connectionInterface class
     */
    public String getConnectionInterface() {
        return connectionInterface;
    }

    /**
     * Setter method for ConnectionInterface property
     *
     * @param connectionInterface connection interface class
     */
    public void setConnectionInterface(String connectionInterface) {
        this.connectionInterface = connectionInterface;
    }

    /**
     * Compare the MCF Config properties in this object with the
     * passed ones
     *
     * @param cdi - The ConnDescInfo object whose MCF config props are to
     *            to be comapred against our props
     * @return true - if the config properties are the same
     *         false otherwise
     */
    public ReconfigAction compareMCFConfigProperties(ConnectorDescriptorInfo cdi) {
        return compareMCFConfigProperties(cdi, new HashSet<>());
    }

    /**
     * Compare the MCF Config properties in this object with the
     * passed ones. The properties in the Set of excluded properties
     * are not compared against
     *
     * @param cdi - The ConnDescInfo object whose MCF config props are to
     *            to be comapred against our props
     * @param excluded - list of properties to be excluded from comparison
     * @return true - if the config properties are the same
     *         false otherwise
     */
    public ReconfigAction compareMCFConfigProperties(ConnectorDescriptorInfo cdi, Set<String> excluded) {
        Set<ConnectorConfigProperty> mcfConfigProps = cdi.getMCFConfigProperties();
        if (mcfConfigProps.size() != mcfConfigProperties.size()) {
            // return false;
            // Cannot determine anything due to size disparity - assume restart
            return ReconfigAction.RECREATE_POOL;
        }

        boolean same = false;

        for (ConnectorConfigProperty mcfConfigProp : mcfConfigProps) {
            // see if this property is in our list of excludes
            if (excluded.contains(mcfConfigProp.getName())) {
                // _logger.finest("mcfProp ignored : " + prop.getName() );
                continue;
            }

            for (ConnectorConfigProperty property : mcfConfigProperties) {
                if (isEnvPropEqual(mcfConfigProp, property)) {
                    // we have a match
                    same = true;
                    // _logger.finest("mcfprop matched : " + prop.getName());
                    break;
                }
            }
            if (!same) {
                //_logger.finest("mcfprop not matched : " + prop.getName() );
                //return false;
                return ReconfigAction.RECREATE_POOL;
            }
            same = false;
        }

        return ReconfigAction.NO_OP;
    }

    /**
     * The ConnectorConfigProperty ::equals method only checks for name equality
     * So we need to write a custom equals
     *
     * @param e1 property
     * @param e2 property
     * @return boolean - equality result
     */
    private boolean isEnvPropEqual(ConnectorConfigProperty e1, ConnectorConfigProperty e2) {
        if (e1 != null && e2 != null && e1.getName() != null && e2.getName() != null
            && e1.getName().equals(e2.getName())) {
            if (e1.getValue() != null && e2.getValue() != null && e1.getValue().equals(e2.getValue())) {
                return true;
            }
        }
        return false;
    }
}
