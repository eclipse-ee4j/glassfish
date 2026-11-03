/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.rave.designtime.markup;

/**
 * Placeholder added to pass compilation since the {@code com.sun.rave.}
 * classes are not available.
 */
public class AttributeDescriptor {

    /**
     * Attribute name.
     */
    private final String name;

    /**
     * Attribute required flag.
     */
    private final boolean required;

    /**
     * Attribute default value.
     */
    private final Object defaultValue;

    /**
     * Attribute bind-able flag.
     */
    private final boolean bindable;

    /**
     * Create a new instance.
     * @param attrName attribute name.
     * @param attrRequired attribute required flag
     * @param attrDefaultValue attribute default value
     * @param attrBindable attribute bind-able flag
     */
    public AttributeDescriptor(final String attrName,
            final boolean attrRequired, final Object attrDefaultValue,
            final boolean attrBindable) {

        this.name = attrName;
        this.required = attrRequired;
        this.defaultValue = attrDefaultValue;
        this.bindable = attrBindable;
    }

    /**
     * Get this attribute name.
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Get the bind-able flag value.
     * @return {@code true} if bind-able, {@code false} otherwise
     */
    public boolean isBindable() {
        return bindable;
    }

    /**
     * Get the required flag value.
     * @return {@code true} if required, {@code false} otherwise
     */
    public boolean isRequired() {
        return required;
    }
}
