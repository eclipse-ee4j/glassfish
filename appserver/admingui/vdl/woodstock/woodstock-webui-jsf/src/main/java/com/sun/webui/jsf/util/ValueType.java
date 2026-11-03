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

package com.sun.webui.jsf.util;

/**
 * This class provides a type safe enumeration of value types (see also
 * ValueTypeEvaluator). The ValueTypeEvaluator and the ValueTypes are helper
 * classes for UIComponents which accept value bindings that can be either
 * single objects or a collection of objects (for example, an array). Typically,
 * these components have to process input differently depending on the type of
 * the value object.
 *
 * @see com.sun.webui.jsf.util.ValueTypeEvaluator
 */
public final class ValueType {

    /**
     * The type.
     */
    private final String type;

    /**
     * Indicates that the value binding is an array (of primitives or of
     * objects).
     */
    public static final ValueType ARRAY = new ValueType("array");

    /**
     * Indicates that the value binding is assignable to a
     * {@code java.util.List}.
     */
    public static final ValueType LIST = new ValueType("list");

    /**
     * Indicates that the value binding is neither an array, nor does it
     * implement {@code java.util.List}.
     */
    public static final ValueType OBJECT = new ValueType("object");

    /**
     * Indicates that the value binding is invalid. This is a place holder,
     * currently the ValueTypeEvaluator does not return this. It should be used
     * to help page authors identify what the valid types are (e.g.
     * {@code java.util.List} works, but {@code java.util.Collection} does not).
     */
    public static final ValueType INVALID = new ValueType("invalid");

    /**
     * Indicates that no value was specified for the component.
     */
    public static final ValueType NONE = new ValueType("none");

    /**
     * Create a new instance.
     * @param valueType type
     */
    private ValueType(final String valueType) {
        type = valueType;
    }

    /**
     * Get a String representation of the action.
     *
     * @return A String representation of the value type.
     */
    @Override
    public String toString() {
        return type;
    }
}
