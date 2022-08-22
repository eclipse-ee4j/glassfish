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

import jakarta.resource.spi.AuthenticationMechanism;
import jakarta.resource.spi.TransactionSupport.TransactionSupportLevel;
import jakarta.resource.spi.security.GenericCredential;
import jakarta.resource.spi.security.PasswordCredential;

import java.util.Set;

import org.glassfish.deployment.common.Descriptor;
import org.ietf.jgss.GSSCredential;

import static com.sun.enterprise.deployment.xml.ConnectorTagNames.DD_LOCAL_TRANSACTION;
import static com.sun.enterprise.deployment.xml.ConnectorTagNames.DD_NO_TRANSACTION;
import static com.sun.enterprise.deployment.xml.ConnectorTagNames.DD_XA_TRANSACTION;
import static jakarta.resource.spi.TransactionSupport.TransactionSupportLevel.LocalTransaction;
import static jakarta.resource.spi.TransactionSupport.TransactionSupportLevel.NoTransaction;
import static jakarta.resource.spi.TransactionSupport.TransactionSupportLevel.XATransaction;

/**
 * Deployment Information for connector outbound-resourceadapter
 *
 * @author Qingqing Ouyang
 * @author Sheetal Vartak
 */
public class OutboundResourceAdapter extends Descriptor {

    private static final long serialVersionUID = 1L;

    private TransactionSupportLevel transactionSupport = LocalTransaction;
    private Set<AuthMechanism> authMechanisms;
    private boolean reauthenticationSupport;
    private final Set<ConnectionDefDescriptor> connectionDefs;

    /*Set variables indicates that a particular attribute is set by DD processing so that
      annotation processing need not (must not) set the values from annotation */
    private boolean reauthenticationSupportSet;
    private boolean transactionSupportSet;

    public OutboundResourceAdapter() {
        this.authMechanisms = new OrderedSet<>();
        this.connectionDefs = new OrderedSet<>();
    }


    /**
     * @return the value of supportsReauthentication
     */
    public boolean supportsReauthentication() {
        return reauthenticationSupport;
    }


    public String getReauthenticationSupport() {
        return String.valueOf(reauthenticationSupport);
    }


    /**
     * Sets the value of supportsReauthentication
     */
    public void setReauthenticationSupport(boolean reauthenticationSupport) {
        this.reauthenticationSupportSet = true;
        this.reauthenticationSupport = reauthenticationSupport;
    }


    /**
     * sets the value of supportsReauthentication
     * DOL rearchitecture
     */
    public void setReauthenticationSupport(String reauthSupport) {
        this.reauthenticationSupport = Boolean.parseBoolean(reauthSupport);
        this.reauthenticationSupportSet = true;
    }


    /**
     * @return NO_TRANSACTION, LOCAL_TRANSACTION, XA_TRANSACTION
     */
    public String getTransSupport() {
        if (transactionSupport == NoTransaction) {
            return DD_NO_TRANSACTION;
        } else if (transactionSupport == LocalTransaction) {
            return DD_LOCAL_TRANSACTION;
        } else {
            return DD_XA_TRANSACTION;
        }
    }


    /**
     * Set value of transactionSupport to NO_TRANSACTION,
     * LOCAL_TRANSACTION, XA_TRANSACTION as defined in {@link TransactionSupportLevel}
     */
    public void setTransactionSupport(String support) {
        if (DD_XA_TRANSACTION.equals(support)) {
            this.transactionSupport = XATransaction;
        } else if (DD_LOCAL_TRANSACTION.equals(support)) {
            this.transactionSupport = LocalTransaction;
        } else {
            this.transactionSupport = NoTransaction;
        }
        this.transactionSupportSet = true;
    }


    /**
     * Set of AuthMechanism objects
     */
    public Set<AuthMechanism> getAuthMechanisms() {
        if (authMechanisms == null) {
            authMechanisms = new OrderedSet<>();
        }
        return authMechanisms;
    }


    /**
     * Add a AuthMechanism object to the set return value :
     *
     * @return false if found, true if not found
     */
    public boolean addAuthMechanism(AuthMechanism mech) {
        for (AuthMechanism next : authMechanisms) {
            if (next.getAuthMechVal() == mech.getAuthMechVal()) {
                return false;
            }
        }
        return this.authMechanisms.add(mech);
    }


    /**
     * Remove a AuthMechanism object to the set
     *
     * @return false if found, true if not found
     */
    public boolean removeAuthMechanism(AuthMechanism mech) {
        for (AuthMechanism next : authMechanisms) {
            if (next.equals(mech)) {
                return this.authMechanisms.remove(mech);
            }
        }
        return false;
    }


    /**
     * Add a AuthMechanism object with given auth mech value to the set
     *
     * @return false if found, true if not found
     */
    public boolean addAuthMechanism(int mech) {
        for (AuthMechanism next : authMechanisms) {
            if (next.getAuthMechVal() == mech) {
                return false;
            }
        }
        AuthMechanism auth = new AuthMechanism(mech);
        return this.authMechanisms.add(auth);
    }


    /**
     * Remove a AuthMechanism object with given auth mech value from the set
     *
     * @return false if found, true if not found
     */
    public boolean removeAuthMechanism(int mech) {
        for (AuthMechanism next : authMechanisms) {
            if (next.getAuthMechVal() == mech) {
                return this.authMechanisms.remove(next);
            }
        }
        return false;
    }


    /**
     * adds an entry to the set of connection definitions
     */
    public void addConnectionDefDescriptor(ConnectionDefDescriptor conDefDesc) {
        this.connectionDefs.add(conDefDesc);
    }


    public boolean hasConnectionDefDescriptor(String connectionFactoryIntf) {
        for (ConnectionDefDescriptor cdd : connectionDefs) {
            if (cdd.getConnectionFactoryIntf().equals(connectionFactoryIntf)) {
                return true;
            }
        }
        return false;
    }


    /**
     * removes an entry from the set of connection definitions
     */
    public void removeConnectionDefDescriptor(ConnectionDefDescriptor conDefDesc) {
        this.connectionDefs.remove(conDefDesc);
    }


    /**
     * @return the set of connection definitions
     */
    public Set<ConnectionDefDescriptor> getConnectionDefs() {
        return connectionDefs;
    }


    /**
     * For being able to read 1.0 and write 1.5
     */
    public void setConnectionDef(ConnectionDefDescriptor conDef) {
        this.connectionDefs.add(conDef);
    }


    public ConnectionDefDescriptor getConnectionDef() {
        return connectionDefs.iterator().next();
    }


    /**
     * @return the value of ManagedconnectionFactoryImpl
     */
    public String getManagedConnectionFactoryImpl() {
        return getConnectionDef().getManagedConnectionFactoryImpl();
    }


    /**
     * Sets the value of ManagedconnectionFactoryImpl
     */
    public void setManagedConnectionFactoryImpl(String managedConnectionFactoryImpl) {
        getConnectionDef().setManagedConnectionFactoryImpl(managedConnectionFactoryImpl);
    }


    /**
     * @return Set of ConnectorConfigProperty
     */
    public Set<ConnectorConfigProperty> getConfigProperties() {
        return getConnectionDef().getConfigProperties();
    }


    /**
     * Add a configProperty to the set
     */
    public void addConfigProperty(ConnectorConfigProperty configProperty) {
        getConnectionDef().getConfigProperties().add(configProperty);
    }


    /**
     * Add a configProperty to the set
     */
    public void removeConfigProperty(ConnectorConfigProperty configProperty) {
        getConnectionDef().getConfigProperties().remove(configProperty);
    }


    /**
     * @return connection factory impl
     */
    public String getConnectionFactoryImpl() {
        return getConnectionDef().getConnectionFactoryImpl();
    }


    /**
     * set connection factory impl
     */
    public void setConnectionFactoryImpl(String cf) {
        getConnectionDef().setConnectionFactoryImpl(cf);
    }


    /**
     * @return connection factory intf
     */
    public String getConnectionFactoryIntf() {
        return getConnectionDef().getConnectionFactoryIntf();
    }


    /**
     * set connection factory intf
     */
    public void setConnectionFactoryIntf(String cf) {
        getConnectionDef().setConnectionFactoryIntf(cf);
    }


    /**
     * @return connection intf
     */
    public String getConnectionIntf() {
        return getConnectionDef().getConnectionIntf();
    }


    /**
     * set connection intf
     */
    public void setConnectionIntf(String con) {
        getConnectionDef().setConnectionIntf(con);
    }


    /**
     * @return connection impl
     */
    public String getConnectionImpl() {
        return getConnectionDef().getConnectionImpl();
    }


    /**
     * set connection intf
     */
    public void setConnectionImpl(String con) {
        getConnectionDef().setConnectionImpl(con);
    }


    public boolean isReauthenticationSupportSet() {
        return reauthenticationSupportSet;
    }


    public boolean isTransactionSupportSet() {
        return transactionSupportSet;
    }


    @SuppressWarnings("deprecation")
    public static String getCredentialInterfaceName(AuthenticationMechanism.CredentialInterface ci) {
        if (ci.equals(AuthenticationMechanism.CredentialInterface.GenericCredential)) {
            return GenericCredential.class.getName();
        } else if (ci.equals(AuthenticationMechanism.CredentialInterface.GSSCredential)) {
            return GSSCredential.class.getName();
        } else if (ci.equals(AuthenticationMechanism.CredentialInterface.PasswordCredential)) {
            return PasswordCredential.class.getName();
        }
        throw new RuntimeException("Invalid credential interface :  " + ci);
    }
}
