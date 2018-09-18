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

package com.sun.enterprise.connectors.authentication;

import java.io.Serializable;

/**
 * This a javabean class thatabstracts the backend principal.
 * The backend principal consist of the userName and password
 * which is used for authenticating/getting connection from
 * the backend.
 *
 * @author Srikanth P
 */
public class EisBackendPrincipal implements Serializable {

    private String userName;
    private String password;

    /**
     * Default constructor
     */
    public EisBackendPrincipal() {
    }

    /**
     * Constructor
     *
     * @param userName UserName
     * @param password Password
     */
    public EisBackendPrincipal(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    /**
     * Setter method for UserName property
     * @param userName UserName
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Setter method for password property
     * @param password Password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Getter method for UserName property
     * @return UserName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Getter method for Password property
     * @return Password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Overloaded method from "Object" class
     * Checks the equality.
     * @param backendPrincipal Backend principal against which equality has to
     * @return true if they are equal
     *         false if hey are not equal.
     */
    public boolean equals(Object backendPrincipal) {

        if (backendPrincipal == null ||
                !(backendPrincipal instanceof EisBackendPrincipal)) {
            return false;
        }
        EisBackendPrincipal eisBackendPrincipal =
                (EisBackendPrincipal) backendPrincipal;

        if (isEqual(eisBackendPrincipal.userName, this.userName) &&
                isEqual(eisBackendPrincipal.password, this.password)) {
            return true;
        } else {
            return false;

        }
    }

    /**
     * Checks whether two strings are equal including the null string
     * cases.
     * @param first  first String
     * @param second second String
     * @return boolean equality status
     */
    private boolean isEqual(String first, String second) {
        if (first == null && second == null) {
            return true;
        }
        if (first == null || second == null) {
            return false;
        }
        return (second.equals(first));
    }

    /**
     * Overloaded method from "Object" class
     * Generates the hashcode
     * @return a hash code value for this object
     */
    public int hashCode() {
        int result = 67;
        if (userName != null)
            result = 67 * result + userName.hashCode();
        if (password != null)
            result = 67 * result + password.hashCode();
        return result;
    }
}
