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

package com.sun.enterprise.deployment;

import org.glassfish.deployment.common.Descriptor;

import java.util.Set;

/**
 * <!ELEMENT adminobject (adminobject-interface, adminobject-class, config-property*)>
 *
 * @author Qingqing Ouyang
 * @author Sheetal Vartak
 */
public class AdminObject extends Descriptor {

    private String theInterface;
    private String theClass;
    private Set configProperties;

    public AdminObject() {
        this.configProperties = new OrderedSet();
    }


    public AdminObject(String theInterface, String theClass) {
        this.theInterface = theInterface;
        this.theClass = theClass;
        this.configProperties = new OrderedSet();
    }


    public String getAdminObjectInterface() {
        return this.theInterface;
    }


    public void setAdminObjectInterface(String intf) {
        this.theInterface = intf;
    }


    public String getAdminObjectClass() {
        return this.theClass;
    }


    public void setAdminObjectClass(String cl) {
        this.theClass = cl;
    }


    /**
     * Set of EnvironmentProperty
     */
    public Set getConfigProperties() {
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
}
