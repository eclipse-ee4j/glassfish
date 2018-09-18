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

package org.glassfish.resources.api;

import com.sun.enterprise.repository.ResourceProperty;

import java.io.Serializable;

public class ResourcePropertyImpl implements ResourceProperty,
    Serializable {

    private String name_;
    private Object value_;

    public ResourcePropertyImpl(String name) {
        name_  = name;
        value_ = null;
    }

    public ResourcePropertyImpl(String name, Object value) {
        name_  = name;
        value_ = value;
    }

    public String getName() {
        return name_;
    }

    public Object getValue() {
        return value_;
    }

    public void setValue(Object value) {
        value_ = value;
    }

    public int hashCode() {
        return name_.hashCode();
    }

    public boolean equals(Object other) {
        boolean equal = false;
        if( other instanceof ResourceProperty ) {
            ResourceProperty otherProp = (ResourceProperty) other;
            equal = this.name_.equals(otherProp.getName());
        }
        return equal;
    }

    public String toString() {
        return "ResourceProperty : < " + getName() + " , " + getValue() +
            " >";
    }
}
