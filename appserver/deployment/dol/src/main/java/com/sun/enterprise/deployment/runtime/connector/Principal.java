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

/**
 *    This generated bean class Principal matches the DTD element principal
 *
 *    Generated on Mon May 13 13:36:48 PDT 2002
 */

package com.sun.enterprise.deployment.runtime.connector;

import com.sun.enterprise.deployment.runtime.RuntimeDescriptor;

/**
 * This class was based on the schema2beans generated one modified
 * to remove its dependencies on schema2beans libraries.

 * @author  Jerome Dochez
 * @version
 */
public class Principal extends RuntimeDescriptor {

    public static final String USER_NAME = "UserName";
    public static final String CREDENTIAL = "Credential";
    public static final String PASSWORD = "Password";

    // This method verifies that the mandatory properties are set
    public boolean verify() {
        return true;
    }

}
