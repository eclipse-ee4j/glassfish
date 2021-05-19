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

/*
 * PoolManagerConstants.java
 *
 * Created on November 5, 2002, 1:58 PM
 */

package com.sun.enterprise.deployment;

/**
 *
 * @author  dochez
 */
public class PoolManagerConstants {

    // transaction support levels
    static public final int NO_TRANSACTION = 0;
    static public final int LOCAL_TRANSACTION = 1;
    static public final int XA_TRANSACTION = 2;

    // Authentication mechanism levels
    static public final int BASIC_PASSWORD = 0;
    static public final int KERBV5 = 1;

    // Credential Interest levels
    static public final String PASSWORD_CREDENTIAL = "jakarta.resource.spi.security.PasswordCredential";
    static public final String GENERIC_CREDENTIAL = "jakarta.resource.spi.security.GenericCredential";

}
