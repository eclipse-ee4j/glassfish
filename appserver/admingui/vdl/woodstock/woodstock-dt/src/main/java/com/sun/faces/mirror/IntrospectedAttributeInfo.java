/*
 * Copyright (c) 2007, 2019 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.faces.mirror;

import com.sun.rave.designtime.markup.AttributeDescriptor;

/**
 * Represents an attribute for a property of a class from a dependent library,
 * discovered using introspection.
 */
public final class IntrospectedAttributeInfo implements AttributeInfo {

    /**
     * Attribute descriptor.
     */
    private final AttributeDescriptor attributeDescriptor;

    /**
     * Attribute description.
     */
    private String description;

    /**
     * Create a new instance.
     * @param attrDescriptor attribute descriptor
     */
    IntrospectedAttributeInfo(final AttributeDescriptor attrDescriptor) {
        this.attributeDescriptor = attrDescriptor;
    }

    @Override
    public String getName() {
        return this.attributeDescriptor.getName();
    }

    @Override
    public boolean isBindable() {
        return this.attributeDescriptor.isBindable();
    }

    @Override
    public boolean isRequired() {
        return this.attributeDescriptor.isRequired();
    }

    @Override
    public String getMethodSignature() {
        return null;
    }

    @Override
    public String getDescription() {
        if (description == null) {
            return "";
        }
        return this.description;
    }

    /**
     * Set the attribute description.
     * @param attrDesc new attribute description
     */
    void setDescription(final String attrDesc) {
        this.description = attrDesc;
    }

    @Override
    public String getWriteMethodName() {
        String name = this.getName();
        return "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}
