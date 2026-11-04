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

/**
 * A base class that defines the basic metadata available for a property,
 * whether it belongs to a class declared in the current compilation unit, or to
 * a class in a dependent library.
 */
public abstract class PropertyInfo extends FeatureInfo {

    /**
     * Returns the name of this property as a Java instance name. This will
     * usually be the value of {@link #getName()}
     *
     * @return String
     */
    public abstract String getInstanceName();

    /**
     * Returns the fully qualified type name of this property.
     *
     * @return String
     */
    public abstract String getType();

    /**
     * Returns the simple name of this property's read method. May be null if
     * property is write-only or if the property is inherited and the read
     * method is defined in the super class.
     *
     * @return String
     */
    public abstract String getReadMethodName();

    /**
     * Returns the simple name of this property's write method. May be null if
     * property is read-only or if the property is inherited and the write
     * method is defined in the super class.
     *
     * @return String
     */
    public abstract String getWriteMethodName();

    /**
     * Returns the fully qualified type name of a property editor class to be
     * used for editing this property.
     *
     * @return String, or {@code null} if not editor was assigned to this
     * property
     */
    public abstract String getEditorClassName();

    /**
     * Get the category info for this property.
     *
     * @return CategoryInfo or {@code null} if this property is not categorized.
     */
    public abstract CategoryInfo getCategoryInfo();

    /**
     * Get the category reference name.
     *
     * @return String
     */
    abstract String getCategoryReferenceName();

    /**
     * Returns the attribute info for this property.
     *
     * @return AttributeInfo or {@code null} if this property does not
     * correspond to an attribute.
     */
    public abstract AttributeInfo getAttributeInfo();

}
