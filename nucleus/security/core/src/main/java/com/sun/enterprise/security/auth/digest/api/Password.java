/*
 * Copyright (c) 2006, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.auth.digest.api;

/**
 * represents plain text password and pre hashed(username+realmname+password) password.
 *
 * @author K.Venuopal@sun.com
 */
public interface Password {
    public static final int PLAIN_TEXT = 0;
    public static final int HASHED = 1;

    /**
     * returns PLAIN_TEXT or HASHED.
     *
     * @returns int
     */
    public int getType();

    /**
     * returns password.
     *
     * @returns byte[]
     */
    public byte[] getValue();
}
