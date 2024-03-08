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

package com.sun.enterprise.admin.util;

/**
 * A class that holds the user and password for the connection to the server. Used by the HttpConnectorAddress class.
 * Instances of this class are immutable.
 */
public final class AuthenticationInfo {
    private final String user;
    private final char[] password;

    /**
     * The only way to construct the instances of this class.
     *
     * @param user the user name for the connection
     * @param password the clear text password for the connection
     */
    public AuthenticationInfo(String user, char[] password) {
        this.user = user;
        this.password = password;
    }

    /**
     * Returns the user name.
     *
     * @return String
     */
    public String getUser() {
        return user;
    }

    /**
     * Returns the password in clear text.
     *
     * @return String
     */
    public char[] getPassword() {
        return password;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("User: ").append(user);
        result.append(", Password: ").append((password != null && password.length > 0) ? "<non-null>" : "<null>");
        return result.toString();
    }
}
