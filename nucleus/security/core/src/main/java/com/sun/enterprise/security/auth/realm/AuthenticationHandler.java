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

package com.sun.enterprise.security.auth.realm;

import java.security.Principal;
import com.sun.enterprise.security.auth.*;

/**
 * This interface is used by the Authentication Service to have the
 * Principal authenticated by the realm. A realm provides an
 * implementation of this interface.
 * @author Harish Prabandham
 * @author Harpreet Singh
 */
public interface AuthenticationHandler {
    /**
     * Returns the Realm that this Authentication Handler is authenticating
     * in.
     * @return The Realm object in which this handler is authenticating in.
     */
    public Realm getRealm();

    /**
     * This method authenticates the given principal using the specified
     * authentication data and the Principal's Credentials. The result of
     * the authentication is returned back.
     * @param The principal (user) being authenticated.
     * @param The data needed for authentication.
     * @return boolean denoting true for success and false for failure
     * authentication.
     */
    public boolean doAuthentication(String principalName,
                                    byte[] authData);
}
