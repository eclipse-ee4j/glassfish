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

package com.sun.enterprise.security.auth.login.common;

import com.sun.enterprise.util.LocalStringManagerImpl;
/**
 * LoginException is thrown by the LoginContext class whenever
 * the following happens: <UL>
 * <LI> If the client is unable to authenticate successfully with the
 * </UL>
 * @see com.sun.enterprise.security.auth.AuthenticationStatus
 * @author Harish Prabandham
 * @author Harpreet Singh
 */

public class LoginException extends SecurityException {

    private static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(LoginException.class);

    private boolean status = false;


    /**
     * Create a new LoginException object with the given message
     * @param The message indicating why authentication failed.
     */
    public LoginException(String message) {
        super(message);
    }


    /**
     * Create a new LoginException object with the given authentication
     * value.
     * @param The AuthenticationStatus object
     */
    public LoginException(boolean as){
        super(localStrings.getLocalString("enterprise.security.login_failed",
                                           "Login Failed."));
        status = as;
    }


    /**
     * Returns the status of the Authentication.
     */
    public boolean getStatus(){
        return status;
    }

}




