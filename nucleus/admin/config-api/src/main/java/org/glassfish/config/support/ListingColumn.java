/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.config.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * List command column annotation.
 *
 * <p>>This annotation works with the Listing annotation to provide additional information about
 * columns in the output. The annotation can be placed on any method that takes no arguments and
 * returns a type that can be converted to a {@link String}, including default methods.
 *
 * @author Tom Mueller
 */
@Retention(RUNTIME)
@Target(ElementType.METHOD)
public @interface ListingColumn {
    /**
     * Determines the order of the columns from left to right. The "key" attribute is assigned order value 0.
     * Higher order values are for columns further to the right.
     */
    int order() default GenericListCommand.ColumnInfo.NONKEY_ORDER;

    /**
     * Returns the header for the column. The calculated value is the method name converted to XML form,
     * e.g., getSomeAttr
     * is SOME-ATTR
     */
    String header() default "";

    /**
     * Determines whether a column should be excluded from the output. The default is false.
     */
    boolean exclude() default false;

    /**
     * Determines whether a column should be included in the --long output by default. The default is true.
     */
    boolean inLongByDefault() default true;
}
