/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.deployment.util.DOLUtils;

import static org.glassfish.deployment.common.JavaEEResourceType.JMSDD;

public class JMSDestinationDefinitionDescriptor extends AbstractConnectorResourceDescriptor {

    private static final long serialVersionUID = 6874534064049457633L;

    // the <description> element will be processed by base class
    private String interfaceName;
    private String className;
    private String destinationName;

    public JMSDestinationDefinitionDescriptor() {
        super();
        super.setResourceType(JMSDD);
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public void addJMSDestinationPropertyDescriptor(ResourcePropertyDescriptor propertyDescriptor){
        getProperties().put(propertyDescriptor.getName(), propertyDescriptor.getValue());
    }

    public boolean isConflict(JMSDestinationDefinitionDescriptor other) {
        return (getName().equals(other.getName())) &&
            !(
                DOLUtils.equals(getInterfaceName(), other.getInterfaceName()) &&
                DOLUtils.equals(getClassName(), other.getClassName()) &&
                DOLUtils.equals(getResourceAdapter(), other.getResourceAdapter()) &&
                DOLUtils.equals(getDestinationName(), other.getDestinationName()) &&
                getProperties().equals(other.getProperties())
            );
    }
}
