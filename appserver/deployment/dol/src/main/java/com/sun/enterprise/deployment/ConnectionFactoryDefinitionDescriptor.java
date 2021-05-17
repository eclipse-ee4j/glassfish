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
import jakarta.resource.spi.TransactionSupport.TransactionSupportLevel;

import static org.glassfish.deployment.common.JavaEEResourceType.*;

/**
 * @author Dapeng Hu
 */
public class ConnectionFactoryDefinitionDescriptor extends AbstractConnectorResourceDescriptor {
    private static final long serialVersionUID = 9173518958930316558L;

    // the <description> element will be processed by base class
    private String interfaceName;
    private String transactionSupport=TransactionSupportLevel.NoTransaction.toString();
    private boolean isTransactionSupportSet = false;
    private int maxPoolSize=-1;
    private int minPoolSize=-1;

    public ConnectionFactoryDefinitionDescriptor() {
        super();
        setResourceType(CFD);
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getTransactionSupport() {
        return transactionSupport;
    }

    public void setTransactionSupport( String transactionSupport) {
        isTransactionSupportSet=true;
        this.transactionSupport = transactionSupport;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getMinPoolSize() {
        return minPoolSize;
    }

    public void setMinPoolSize(int minPoolSize) {
        this.minPoolSize = minPoolSize;
    }

    public boolean isTransactionSupportSet() {
        return isTransactionSupportSet;
    }

    public void addConnectionFactoryPropertyDescriptor(ResourcePropertyDescriptor propertyDescriptor){
        getProperties().put(propertyDescriptor.getName(), propertyDescriptor.getValue());
    }

    public boolean isConflict(ConnectionFactoryDefinitionDescriptor other) {
        return (getName().equals(other.getName())) &&
            !(
                DOLUtils.equals(getInterfaceName(), other.getInterfaceName()) &&
                DOLUtils.equals(getResourceAdapter(), other.getResourceAdapter()) &&
                DOLUtils.equals(getTransactionSupport(), other.getTransactionSupport()) &&
                DOLUtils.equals(getMaxPoolSize(), other.getMaxPoolSize()) &&
                DOLUtils.equals(getMinPoolSize(), other.getMinPoolSize()) &&
                getProperties().equals(other.getProperties())
            );
    }
}
