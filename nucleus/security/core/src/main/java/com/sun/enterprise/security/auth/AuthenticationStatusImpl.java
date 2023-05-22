/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.auth;

/**
 * This class implements an AuthenticationStatus object.
 *
 * @author Harish Prabandham
 */

public class AuthenticationStatusImpl implements AuthenticationStatus {

    private String realmName; // Name of the Realm
    private String authMethod; // Method used for Authentication.
    private String principalName; // String form of the Principal.
    private int status; // Status

    /**
     * This constructs a new AuthenticationStatus object.
     *
     * @param The name of the principal
     * @param The name of the realm that authenticated the principal
     * @param The method used for authenticating the principal
     * @param The status of the authentication
     */
    public AuthenticationStatusImpl(String principalName, String authMethod, String realm, int status) {
        this.principalName = principalName;
        this.authMethod = authMethod;
        this.status = status;
        this.realmName = realm;
    }

    /**
     * This method returns the status of the authentication
     *
     * @return An integer value indicating the status of the authentication
     */
    @Override
    public int getStatus() {
        return status;
    }

    /**
     * This method returns a byte array of zero length, since there's no continuation data needed for passphrase based
     * authentication.
     *
     * @return A byte array of zero length.
     */
    @Override
    public byte[] getContinuationData() {
        return new byte[0];
    }

    /**
     * This method returns a byte array of zero length, since there's no auth specific data needed for passphrase based
     * authentication.
     *
     * @return A byte array of zero length.
     */
    @Override
    public byte[] getAuthSpecificData() {
        return new byte[0];
    }

    /**
     * This method returns the name of realm where the authentication was performed.
     *
     * @return A java.lang.String representation of the realm.
     */
    public String getRealmName() {
        return realmName;
    }

    /**
     * This method returns the "method" used to perform authentication
     *
     * @return A java.lang.String representation of the method used. In passphrase based authentication it returns the
     * string "password".
     */
    public String getAuthMethod() {
        return authMethod;
    }

    /**
     * This method returns the string representation of the principal that was authenticated.
     *
     * @return A java.lang.String representation of the Principal.
     */
    public String getPrincipalName() {
        return principalName;
    }
}
