/*
 * Copyright (c) 2022, 2026 Contributors to the Eclipse Foundation
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

import org.glassfish.deployment.common.Descriptor;

/**
 * connector1.5
 * <!ELEMENT messagelistener (messagelistener-type, activationspec)>
 *
 * @author Sheetal Vartak
 */
public class MessageListener extends Descriptor implements ConnectorConfigPropertySetDescriptor {

    private static final long serialVersionUID = 2L;
    private String msgListenerType;
    private String activationSpecClass;
    private final OrderedSet<ConnectorConfigProperty> configProperties;
    private final OrderedSet<EnvironmentProperty> requiredConfigProperties;

    // default constructor
    public MessageListener() {
        this.configProperties = new OrderedSet<>();
        this.requiredConfigProperties = new OrderedSet<>();
    }


    public String getMessageListenerType() {
        return msgListenerType;
    }


    public void setMessageListenerType(String msgListenerType) {
        this.msgListenerType = msgListenerType;
    }


    public String getActivationSpecClass() {
        return activationSpecClass;
    }


    public void setActivationSpecClass(String activationSpecClass) {
        this.activationSpecClass = activationSpecClass;
    }


    @Override
    public void addConfigProperty(ConnectorConfigProperty configProperty) {
        this.configProperties.add(configProperty);
    }


    @Override
    public void removeConfigProperty(ConnectorConfigProperty configProperty) {
        this.configProperties.remove(configProperty);
    }


    @Override
    public OrderedSet<ConnectorConfigProperty> getConfigProperties() {
        return configProperties;
    }


    /**
     * add a configProperty to the set
     */
    public void addRequiredConfigProperty(EnvironmentProperty configProperty) {
        this.requiredConfigProperties.add(configProperty);
    }


    /**
     * remove a configProperty from the set
     */
    public void removeRequiredConfigProperty(EnvironmentProperty configProperty) {
        this.requiredConfigProperties.remove(configProperty);
    }


    /**
     * @return Set of EnvironmentProperty
     */
    public OrderedSet<EnvironmentProperty> getRequiredConfigProperties() {
        return requiredConfigProperties;
    }
}
