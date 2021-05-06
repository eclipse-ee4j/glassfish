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

package org.glassfish.security.common;

/**
 * In EJBs, ACL checking is done using the Roles. Roles are an abstraction
 * of an application specific Logical Principals. These Principals do not
 * have any properties of Principals within a Security Domain (or Realm).
 * They merely serve as abstraction to application specific entities.
 * @author Harish Prabandham
 */
public class Role extends PrincipalImpl {

    private String description;

    /** Creates a new Role with a given name */
    public Role(String name) {
        super(name);
    }


    public boolean equals(Object other) {
        boolean ret = false;
        if(other instanceof Role) {
            ret =  getName().equals(((Role)other).getName());
        }

        return ret;
    }
    public int hashCode() {
        return getName().hashCode();
    }

    public String getDescription() {
        if (this.description == null) {
            this.description = "";
        }
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

