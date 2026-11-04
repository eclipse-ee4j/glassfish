/*
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that groups together information about the JSP attribute for a
 * component property. Instances of this annotation may be used in two ways: as
 * a value for {@link Property#attribute}, to specify non-default attribute
 * settings as part of a property annotation within a JSF component class; or,
 * to annotate directly the attribute setter methods in a JSP tag class.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.METHOD})
public @interface Attribute {

    /**
     * This attribute's unique name. The name must be unique within the scope of
     * the attribute's component class. If no value is specified, then the name
     * of the containing property will be used.
     *
     * @return String
     */
    String name() default "";

    /**
     * Indicates whether the value of this attribute may be a value-binding
     * expression. If the value of this element is false, the type of the
     * attribute will be set to the property type. If the value is true, it will
     * be set to {@link jakarta.el.ValueExpression}.
     *
     * @return {@code true} if bind-able, {@code false} otherwise
     */
    boolean isBindable() default true;

    /**
     * Indicates whether a value is required for this attribute.
     *
     * @return {@code true} if required, {@code false} otherwise
     */
    boolean isRequired() default false;
}
