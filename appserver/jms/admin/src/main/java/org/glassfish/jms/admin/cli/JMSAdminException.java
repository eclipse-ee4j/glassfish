/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.jms.admin.cli;


/**
 * JMS Admin command failure.
 */
public class JMSAdminException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs an JMSAdminException object
     */
    public JMSAdminException() {
        super();
    }


    /**
     * Constructs an JMSAdminException object
     *
     * @param message Exception message
     */
    public JMSAdminException(String message) {
        super(message);
    }


    /**
     * Constructs an JMSAdminException object
     *
     * @param message Exception message
     * @param cause original cause.
     */
    public JMSAdminException(String message, Exception cause) {
        super(message, cause);
    }


    /**
     * Gets the exception linked to this one
     *
     * @return same as {@link #getCause()}
     */
    @Deprecated(forRemoval = true)
    public Exception getLinkedException() {
        return (Exception) getCause();
    }


    /**
     * Calls {@link #initCause(Throwable)}
     *
     * @param ex the linked Exception
     */
    @Deprecated(forRemoval = true)
    public void setLinkedException(Exception ex) {
        super.initCause(ex);
    }
}
