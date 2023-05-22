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
 * This interface stores the status of the authentication.
 *
 * @author Harish Prabandham
 */

public interface AuthenticationStatus extends java.io.Serializable {
    public static final int AUTH_SUCCESS = 0; // Authentication Successful
    public static final int AUTH_FAILURE = 1; // Authentication Failed
    public static final int AUTH_CONTINUE = 2; // Continue the Authentication
    public static final int AUTH_EXPIRED = 3; // Credentials have expired.

    /**
     * This method returns the status of the authentication
     *
     * @return An integer value indicating the status of the authentication
     */
    int getStatus();

    /**
     * This is the value returned by the Authenticator when the status is AUTH_CONTINUE. This data should give an indication
     * to the client on what else it should send to the server to complete the authentication.
     *
     * @return An array of bytes indicating the additional information needed to complete the authentication.
     */
    byte[] getContinuationData();

    /**
     * This is the value returned by the Authenticator when the status is AUTH_CONTINUE. This data should give an indication
     * to the client on specific authentication it needs to adopt to continue on with the authentication.
     *
     * @return An array of bytes indicating the authentication specific information needed to complete the authentication.
     */
    byte[] getAuthSpecificData();
}
