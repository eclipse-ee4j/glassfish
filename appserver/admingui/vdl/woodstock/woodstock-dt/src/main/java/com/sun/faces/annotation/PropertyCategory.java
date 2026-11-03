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
package com.sun.faces.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that identifies a public field as being a property category
 * instance. The field must be assignable from
 * {@link com.sun.rave.designtime.CategoryDescriptor}. Property annotations may
 * reference annotated property categories. For example, the property annotation
 *
 * <pre>
 *    &#64;Property(name="myProperty",category="myCategory")
 *    private String myProperty;
 * </pre>
 *
 * contains a reference to the property category whose name is
 * {@code myCategory}, which might be defined elsewhere as
 *
 * <pre>
 *    &#64;PropertyCategory("myCategory")
 *    public static final CategoryDescriptor MYCATEGORY =
 *        new CategoryDescriptor("myCategory", "My Category", false);
 * </pre>
 *
 * {@code PropertyCategory} must be used to annotate only publicly accessible
 * fields or static methods of type
 * {@link com.sun.rave.designtime.CategoryDescriptor}.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface PropertyCategory {

    /**
     * This category's unique name. The name must be unique within the scope of
     * the current compilation unit. This name should generally be exactly equal
     * to the value of {@code CategoryDescriptor.getName()}. If it is not,
     * category descriptors inherited from external libraries will not be
     * comparable to category descriptors defined in the current compilation
     * unit.
     *
     * @return String
     */
    String name();

    /**
     * This category's sort key, used to determine the order in which this
     * category should appear in a list of categories. If not specified, the
     * sort key defaults to the value of {@link #name}.
     *
     * <p>
     * The collation sequence used for ordering is not specified.
     *
     * @return String
     */
    String sortKey() default "";

}
