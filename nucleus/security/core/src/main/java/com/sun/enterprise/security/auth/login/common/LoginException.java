/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.security.auth.login.common;

/**
 * LoginException is thrown by the LoginContext class if the client is unable to authenticate successfully.
 *
 * @see jakarta.security.enterprise.AuthenticationStatus
 * @author Harish Prabandham
 * @author Harpreet Singh
 */
public class LoginException extends SecurityException {

    private static final long serialVersionUID = -3371991085029706070L;

    /**
     * Create a new LoginException object with the given message
     *
     * @param message indicating why authentication failed.
     */
    public LoginException(String message) {
        super(message);
    }

    /**
     * Create a new LoginException object with the given message
     *
     * @param message indicating why authentication failed.
     * @param cause
     */
    public LoginException(String message, Exception cause) {
        super(message, cause);
    }
}
