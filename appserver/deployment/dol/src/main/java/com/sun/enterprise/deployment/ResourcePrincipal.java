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

package com.sun.enterprise.deployment;

import java.util.Objects;

import org.glassfish.security.common.PrincipalImpl;

/**
 * This class encapsulates the Resource Principal information needed
 * to access the Resource.
 *
 * @author Tony Ng
 */
public class ResourcePrincipal extends PrincipalImpl {

    private static final long serialVersionUID = 1L;

    private final String password;

    public ResourcePrincipal(String name, String password) {
        super(name);
        this.password = password;
    }


    public String getPassword() {
        return password;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o instanceof ResourcePrincipal) {
            ResourcePrincipal other = (ResourcePrincipal) o;
            return Objects.equals(getName(), other.getName()) && Objects.equals(this.password, other.password);
        }
        return false;
    }


    @Override
    public int hashCode() {
        return Objects.hash(getName(), password);
    }
}
