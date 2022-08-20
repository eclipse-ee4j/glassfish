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

package org.glassfish.resources.api;

import com.sun.enterprise.repository.ResourceProperty;

import java.io.Serializable;

public class ResourcePropertyImpl implements ResourceProperty, Serializable {

    private static final long serialVersionUID = 1L;
    private final String name;
    private String value;

    public ResourcePropertyImpl(String name) {
        this.name = name;
        this.value = null;
    }


    public ResourcePropertyImpl(String name, String value) {
        this.name = name;
        this.value = value;
    }


    @Override
    public String getName() {
        return name;
    }


    @Override
    public String getValue() {
        return value;
    }


    @Override
    public void setValue(String value) {
        this.value = value;
    }


    @Override
    public int hashCode() {
        return name.hashCode();
    }


    @Override
    public boolean equals(Object other) {
        boolean equal = false;
        if (other instanceof ResourceProperty) {
            ResourceProperty otherProp = (ResourceProperty) other;
            equal = this.name.equals(otherProp.getName());
        }
        return equal;
    }


    @Override
    public String toString() {
        return "ResourceProperty : < " + getName() + " , " + getValue() + " >";
    }
}
