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

package org.glassfish.api.admin.config;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.jvnet.hk2.config.DataType;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Describes properties or system properties that might exist as sub-elements.
 */
@Retention(SOURCE) // not needed at runtime until we have something to make use of it
@Target({ TYPE })
@Documented
public @interface PropertyDesc {
    /** name of the property */
    String name();

    /** default value of the property */
    String defaultValue() default "\u0000";

    /** freeform description */
    String description() default "\u0000";

    /** the DataType class, can be Class&lt;? extends {@link DataType}> or String.class, Integer.class, etc */
    Class dataType() default String.class;

    /**
     * Possible values, might not be a complete list and/or there could be other alternatives such as specific numbers,
     * variables, etc.
     */
    String[] values() default {};
}
