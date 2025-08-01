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
 * Superclass for all domain management exceptions.
 */
public class DomainException extends RepositoryException {
    private static final long serialVersionUID = -875053117161725741L;

    /**
     * Constructs a new DomainException object.
     *
     * @param message
     */
    public DomainException(String message) {
        super(message);
    }

    /**
     * Constructs a new DomainException object.
     *
     * @param cause
     */
    public DomainException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new DomainException object.
     *
     * @param message
     * @param cause
     */
    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
