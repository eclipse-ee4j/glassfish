/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.jauth;

/**
 * Authentication failed.
 *
 * <p>
 * This exception is thrown by an AuthModule when authentication failed. This exception is only thrown when the module
 * has updated the response message in the AuthParam.
 *
 * @version %I%, %G%
 */
public class FailureException extends AuthException {

    private static final long serialVersionUID = -6634814390418917726L;

    /**
     * Constructs a FailureException with no detail message. A detail message is a String that describes this particular
     * exception.
     */
    public FailureException() {
    }

    /**
     * Constructs a FailureException with the specified detail message. A detail message is a String that describes this
     * particular exception.
     *
     * @param msg the detail message.
     */
    public FailureException(String msg) {
        super(msg);
    }
}
