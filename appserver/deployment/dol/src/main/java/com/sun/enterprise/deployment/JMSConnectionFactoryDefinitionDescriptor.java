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

import static org.glassfish.deployment.common.JavaEEResourceType.JMSCFDD;

public class JMSConnectionFactoryDefinitionDescriptor extends AbstractConnectorResourceDescriptor {

    private static final long serialVersionUID = 794492878801534084L;

    // the <description> element will be processed by base class
    private String interfaceName;
    private String className;
    private String user;
    private String password;
    private String clientId;
    private boolean transactional = true;
    private int maxPoolSize = -1;
    private int minPoolSize = -1;

    private boolean transactionSet = false;

    public JMSConnectionFactoryDefinitionDescriptor() {
        super();
        super.setResourceType(JMSCFDD);
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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public boolean isTransactional() {
        return transactional;
    }

    public void setTransactional(boolean transactional) {
        this.transactional = transactional;
        setTransactionSet(true);
    }

    public boolean isTransactionSet() {
        return transactionSet;
    }

    public void setTransactionSet(boolean value) {
        this.transactionSet = value;
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

    public void addJMSConnectionFactoryPropertyDescriptor(ResourcePropertyDescriptor propertyDescriptor){
        getProperties().put(propertyDescriptor.getName(), propertyDescriptor.getValue());
    }

    public boolean isConflict(JMSConnectionFactoryDefinitionDescriptor other) {
        return (getName().equals(other.getName())) &&
            !(
                DOLUtils.equals(getInterfaceName(), other.getInterfaceName()) &&
                DOLUtils.equals(getClassName(), other.getClassName()) &&
                DOLUtils.equals(getResourceAdapter(), other.getResourceAdapter()) &&
                DOLUtils.equals(getUser(), other.getUser()) &&
                DOLUtils.equals(getPassword(), other.getPassword()) &&
                DOLUtils.equals(getClientId(), other.getClientId()) &&
                getProperties().equals(other.getProperties()) &&
                isTransactional() == other.isTransactional() &&
                getMinPoolSize() == other.getMinPoolSize() &&
                getMaxPoolSize() == other.getMaxPoolSize()
            );
    }
}
