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

package com.sun.enterprise.deployment;

import java.util.Set;

import org.glassfish.deployment.common.Descriptor;

/**
 * Deployment Information for connection-definition
 *
 * <!ELEMENT connection-definition (
 * managedconnectionfactory-class, connectionfactory-intf,
 * connection-intf, config-property*, connectionfactory-impl, connection-impl
 * )>
 *
 * @author Sheetal Vartak
 */
public class ConnectionDefDescriptor extends Descriptor {

    private static final long serialVersionUID = 1L;
    private final Set<ConnectorConfigProperty> configProperties = new OrderedSet<>();
    private String managedConnectionFactoryImpl = "";
    private String connectionIntf = "";
    private String connectionImpl = "";
    private String connectionfactoryImpl = "";
    private String connectionfactoryIntf = "";

    /**
     * @return the value of ManagedconnectionFactoryImpl
     */
    public String getManagedConnectionFactoryImpl() {
        return managedConnectionFactoryImpl;
    }


    /**
     * Sets the value of ManagedconnectionFactoryImpl
     */
    public void setManagedConnectionFactoryImpl(String managedConnectionFactoryImpl) {
        this.managedConnectionFactoryImpl = managedConnectionFactoryImpl;
    }


    /**
     * Set of ConnectorConfigProperty
     */
    public Set<ConnectorConfigProperty> getConfigProperties() {
        return configProperties;
    }


    /**
     * Add a configProperty to the set
     */
    public void addConfigProperty(ConnectorConfigProperty configProperty) {
        configProperties.add(configProperty);
    }


    /**
     * Add a configProperty to the set
     */
    public void removeConfigProperty(ConnectorConfigProperty configProperty) {
        configProperties.remove(configProperty);
    }


    /**
     * Get connection factory impl
     */
    public String getConnectionFactoryImpl() {
        return connectionfactoryImpl;
    }


    /**
     * set connection factory impl
     */
    public void setConnectionFactoryImpl(String cf) {
        connectionfactoryImpl = cf;
    }


    /**
     * Get connection factory intf
     */
    public String getConnectionFactoryIntf() {
        return connectionfactoryIntf;
    }


    /**
     * set connection factory intf
     */
    public void setConnectionFactoryIntf(String cf) {
        connectionfactoryIntf = cf;
    }


    /**
     * Get connection intf
     */
    public String getConnectionIntf() {
        return connectionIntf;
    }


    /**
     * set connection intf
     */
    public void setConnectionIntf(String con) {
        connectionIntf = con;
    }


    /**
     * Get connection impl
     */
    public String getConnectionImpl() {
        return connectionImpl;
    }


    /**
     * set connection intf
     */
    public void setConnectionImpl(String con) {
        connectionImpl = con;
    }
 }
