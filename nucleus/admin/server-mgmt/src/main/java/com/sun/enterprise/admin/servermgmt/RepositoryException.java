/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.admin.servermgmt;

/**
 *
 * @author kebbs
 */
public class RepositoryException extends Exception {

    private static final long serialVersionUID = 6453511878361248508L;

    /**
     * Constructs a new InstanceException object.
     *
     * @param message
     */
    public RepositoryException(String message) {
        super(message);
    }

    /**
     * Constructs a new InstanceException object.
     *
     * @param cause
     */
    public RepositoryException(Throwable cause) {
        //When created without a message, we take the message of our cause
        this(cause.getLocalizedMessage(), cause);
    }

    /**
     * Constructs a new InstanceException object.
     *
     * @param message
     * @param cause
     */
    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

    private static final String PREFIX = "( ";
    private static final String POSTFIX = " )";

    private String format(String msg, String causeMsg, Throwable cause) {
        if (cause != null) {
            if (msg == null) {
                msg = causeMsg == null ? cause.toString() : causeMsg;
            } else if (causeMsg != null && !causeMsg.equals(msg)) {
                msg += PREFIX + causeMsg + POSTFIX;
            } else {
                msg += PREFIX + cause + POSTFIX;
            }
        }
        return msg;
    }

    /**
     * If there is a cause, appends the getCause().getMessage() to the original message.
     */
    @Override
    public String getMessage() {
        String msg = super.getMessage();
        Throwable cause = super.getCause();
        if (cause != null) {
            msg = format(msg, cause.getMessage(), cause);
        }
        return msg;
    }

    /**
     * If there is a cause, appends the getCause().getMessage() to the original message.
     */
    @Override
    public String getLocalizedMessage() {
        String msg = super.getLocalizedMessage();
        Throwable cause = super.getCause();
        if (cause != null) {
            msg = format(msg, cause.getLocalizedMessage(), cause);
        }
        return msg;
    }
}
