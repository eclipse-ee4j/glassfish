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

import com.sun.enterprise.deployment.xml.ConnectorTagNames;
import org.glassfish.deployment.common.Descriptor;

/**
 * This class encapsulates the xml tags: description, auth-mech-type and
 * credential-interface in the connector specification.
 * @author Sheetal Vartak
 */
public class AuthMechanism extends Descriptor {

    private int authMechVal;
    private String credInterface;

    /**
     * Default constructor.
     */
    public AuthMechanism(){}

    /**
     * Initializes the data members.
     * @param description description
     * @param authMechVal authentication mechanism type.
     * @param credInterface credential interface type.
     */
    public AuthMechanism(String description, int authMechVal,
                         String credInterface) {
        super.setDescription(description);
        this.authMechVal = authMechVal;
        this.credInterface = credInterface;
    }

    /**
     * Set the credential interface.
     * @param cred the interface.
     */
    public void setCredentialInterface(String cred) {
        credInterface = cred;
    }

    /**
     * Get the credential interface.
     * @return credInterface the interface.
     */
    public String getCredentialInterface() {
        return credInterface;
    }

   /**
    * Get the description
    * @return description.
    */
    @Override
    public String getDescription(){
        return super.getDescription();
    }

    /**
     * Sets the description
     * @param description.
     */
    @Override
    public void setDescription(String description){
        super.setDescription(description);
    }

   /**
    * Get the auth-mech-type
    * @return authMechVal the authentication mechanism type
    */
    public String getAuthMechType() {
        if(authMechVal == PoolManagerConstants.BASIC_PASSWORD) {
            return ConnectorTagNames.DD_BASIC_PASSWORD;
        } else {
            return ConnectorTagNames.DD_KERBEROS;
        }
    }

    public static int getAuthMechInt(String value) {
        if (value.trim().equals(ConnectorTagNames.DD_BASIC_PASSWORD)) {
            return PoolManagerConstants.BASIC_PASSWORD;
        } else if ((value.trim()).equals(ConnectorTagNames.DD_KERBEROS)) {
            return PoolManagerConstants.KERBV5;
        } else {
            throw new IllegalArgumentException("Invalid auth-mech-type");
        }
    }

    /**
     * Get the authentication mechanism value.
     */
    public int getAuthMechVal() {
        return authMechVal;
    }

    /**
     * Set the authentication mechanism value.
     */
    public void setAuthMechVal(int value) {
        authMechVal = value;
    }

    /**
     * Set the authentication mechanism value.
     */
    public void setAuthMechVal(String value) {
        if ((value.trim()).equals(ConnectorTagNames.DD_BASIC_PASSWORD)) {
            authMechVal = PoolManagerConstants.BASIC_PASSWORD;
        } else if ((value.trim()).equals(ConnectorTagNames.DD_KERBEROS)) {
            authMechVal = PoolManagerConstants.KERBV5;
        } else {
            throw new IllegalArgumentException("Invalid auth-mech-type");
        }
    }
}
