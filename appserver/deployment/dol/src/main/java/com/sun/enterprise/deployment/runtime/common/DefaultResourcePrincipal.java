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

package com.sun.enterprise.deployment.runtime.common;

import com.sun.enterprise.deployment.runtime.RuntimeDescriptor;

/**
* this class is a shameful duplication of info found in normal descriptors
* it was kept to be backward compatible with the schema2beans descriptors
* generated by iAS 7.0 engineering team.
*
* @author Jerome Dochez
*/
public class DefaultResourcePrincipal extends RuntimeDescriptor {

    static public final String NAME = "Name"; // NOI18N
    static public final String PASSWORD = "Password"; // NOI18N

    // This attribute is mandatory
    @Override
    public void setName(String value) {
        this.setValue(NAME, value);
    }


    //
    @Override
    public String getName() {
        return (String) this.getValue(NAME);
    }


    // This attribute is mandatory
    public void setPassword(String value) {
        this.setValue(PASSWORD, value);
    }


    //
    public String getPassword() {
        return (String) this.getValue(PASSWORD);
    }


    // This method verifies that the mandatory properties are set
    public boolean verify() {
        return true;
    }
}