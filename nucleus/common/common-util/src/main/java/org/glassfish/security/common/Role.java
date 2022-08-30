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

package org.glassfish.security.common;

import java.io.Serializable;
import java.security.Principal;
import java.util.Objects;

/**
 * In EJBs, ACL checking is done using the Roles.
 * <p>
 * Roles are an abstraction of an application specific Logical Principals.
 * These Principals do not have any properties of Principals within a Security Domain (or Realm).
 * They merely serve as abstraction to application specific entities.
 *
 * @author Harish Prabandham
 */
public class Role implements Principal, Serializable {

    private static final long serialVersionUID = 1L;
    private final String name;
    private final String description;

    /**
     * Creates a new Role with a given name
     *
     * @param name cannot be null
     */
    public Role(final String name) {
        this.name = Objects.requireNonNull(name);
        this.description = null;
    }


    /**
     * Creates a new Role with a given name
     *
     * @param name cannot be null
     * @param description can be null
     */
    public Role(final String name, final String description) {
        this.name = Objects.requireNonNull(name);
        this.description = description;
    }


    /**
     * @return role name
     */
    @Override
    public String getName() {
        return this.name;
    }


    /**
     * @return description of the role. Can be null.
     */
    public String getDescription() {
        return this.description;
    }


    /**
     * @return true if the object is an instance of {@link Role} with the same name.
     */
    @Override
    public boolean equals(final Object other) {
        if (other instanceof Role) {
            return getName().equals(((Role) other).getName());
        }
        return false;
    }


    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    /**
     * @return same as {@link #getName()}
     */
    @Override
    public String toString() {
        return getName();
    }
}
