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

package com.sun.enterprise.security;

/**
 * LoginManager needs implementations of this class for accessing the
 * username and passwords.
 * Implementations of LoginDialog can use either character based terminal,
 * GUI, or any other form of querying to get hold of the username & password
 * from the user.
 *
 * @author Harish Prabandham
 */
public interface LoginDialog {
    /**
     * @return The username of the user.
     */
    public String getUserName();
    /**
     *@return The password of the user in plain text...
     */
    public char[] getPassword();
}
