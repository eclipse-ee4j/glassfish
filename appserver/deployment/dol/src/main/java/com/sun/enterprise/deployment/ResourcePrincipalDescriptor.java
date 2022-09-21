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

import com.sun.enterprise.deployment.runtime.common.PrincipalNameDescriptor;

import jakarta.resource.spi.security.PasswordCredential;

import java.util.Objects;

import org.glassfish.security.common.UserNameAndPassword;

/**
 * This class encapsulates the Resource Principal information needed
 * to access the Resource.
 *
 * @author Tony Ng
 */
public class ResourcePrincipalDescriptor extends PrincipalNameDescriptor {

    private static final long serialVersionUID = 1L;

    private final String password;

    public static ResourcePrincipalDescriptor from(UserNameAndPassword principal) {
        return new ResourcePrincipalDescriptor(principal.getName(), principal.getStringPassword());
    }


    public ResourcePrincipalDescriptor(String name, String password) {
        super(name);
        this.password = password;
    }


    public final String getPassword() {
        return password;
    }


    public final boolean hasPassword() {
        return password != null;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o instanceof ResourcePrincipalDescriptor) {
            ResourcePrincipalDescriptor other = (ResourcePrincipalDescriptor) o;
            return Objects.equals(getName(), other.getName()) && Objects.equals(this.password, other.password);
        }
        return false;
    }


    @Override
    public int hashCode() {
        return Objects.hash(getName(), password);
    }


    public UserNameAndPassword toPrincipalNameAndPassword() {
        return new UserNameAndPassword(getName(), password);
    }


    /**
     * @return null if the password is not set
     */
    public PasswordCredential toPasswordCredential() {
        if (password == null) {
            return null;
        }
        return new PasswordCredential(getName(), password.toCharArray());
    }
}
