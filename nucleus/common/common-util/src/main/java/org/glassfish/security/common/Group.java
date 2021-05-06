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
 * This class implements the EJB concept of a Group. A Group is
 * a privilege attribute that several Principals share. Or, in
 * in other words, several Principals belong to a same group.
 *
 * @author Harish Prabandham
 */
public class Group extends PrincipalImpl {
    /** Creates a new Group attribute */
    public Group(String name) {
        super(name);
    }

    public boolean equals(Object other) {
        if(other instanceof Group) {
            return getName().equals(((Group)other).getName());
        } else {
            return false;
        }
    }
    public int hashCode() {
        return getName().hashCode();
    }
}
