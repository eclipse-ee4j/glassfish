/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.gjc.spi;

import jakarta.resource.spi.ConnectionRequestInfo;

import java.util.Arrays;

/**
 * ConnectionRequestInfo implementation for Generic JDBC Connector.
 *
 * @author Binod P.G
 * @version 1.0, 02/07/31
 */
public class ConnectionRequestInfoImpl implements ConnectionRequestInfo {

    private String user;
    private char[] password;

    /**
     * Constructs a new <code>ConnectionRequestInfoImpl</code> object
     *
     * @param user User Name.
     * @param password Password
     */
    public ConnectionRequestInfoImpl(String user, char[] password) {
        this.user = user;
        this.password = password;
    }

    /**
     * Retrieves the user name of the ConnectionRequestInfo.
     *
     * @return User name of ConnectionRequestInfo.
     */
    public String getUser() {
        return user;
    }

    /**
     * Retrieves the password of the ConnectionRequestInfo.
     *
     * @return Password of ConnectionRequestInfo.
     */
    public char[] getPassword() {
        return password;
    }

    /**
     * Verify whether two ConnectionRequestInfoImpls are equal.
     *
     * @return True, if they are equal and false otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj instanceof ConnectionRequestInfoImpl) {
            ConnectionRequestInfoImpl other = (ConnectionRequestInfoImpl) obj;
            return (isEqual(this.user, other.user) && Arrays.equals(this.password, other.password));
        }

        return false;
    }

    /**
     * Retrieves the hashcode of the object.
     *
     * @return hashCode.
     */
    public int hashCode() {
        String result = "" + user + new String(password);
        return result.hashCode();
    }

    /**
     * Compares two objects.
     *
     * @param o1 First object.
     * @param o2 Second object.
     */
    private boolean isEqual(Object o1, Object o2) {
        if (o1 == null) {
            return (o2 == null);
        }

        return o1.equals(o2);
    }

}
