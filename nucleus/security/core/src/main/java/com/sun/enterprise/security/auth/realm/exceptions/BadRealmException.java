/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.security.auth.realm.exceptions;

/**
 * Exception thrown when a Realm is found to be corrupted for some reason. This usually reflects some sort of problem with data
 * structures such as those holding information about users, groups, or ACLs.
 *
 * @author Harish Prabandham
 */
public class BadRealmException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs the exception, with descriptive information.
     *
     * @param info describes the problem with the realm
     */
    public BadRealmException(String info) {
        super(info);
    }

    public BadRealmException() {
        super();
    }

    public BadRealmException(Throwable cause) {
        super(cause);
    }

    public BadRealmException(String info, Throwable cause) {
        super(info, cause);
    }
}
