/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.config;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Indicates that this property or the field value must be injected from
 * an XML attribute in a configuration file.
 *
 * @author Kohsuke Kawaguchi
 * @see Element
 */
@Retention(RUNTIME)
@Target({FIELD,METHOD})
public @interface Attribute {
    /**
     * Attribute name.
     *
     * <h2>Default Name</h2>
     * <p>
     * If this value is omitted, the default name is inferred from the field/method name.
     * First, if this is a method and the name starts with "set", then "set" will be trimmed
     * off.
     *
     * <p>
     * Then names are tokenized according to the camel case word separator,
     * then tokens are combined with '-', then finally the whole thing is converted
     * to the lower case.
     *
     * <p>
     * Therefore, for example, a field name "httpBufferSize" would yield
     * "http-buffer-size", and a method name "setThreadCount" would yield
     * "thread-count"
     */
    String value() default "";

    /**
     * Indicates that this property becomes the name of the component.
     * There can be only one key on a class.
     */
    boolean key() default false;

    /**
     * Indicates that this attribute is required.
     *
     * <p>
     * To specify the default value, simply use the field initializer
     * to set it to a certain value. The field/method values are only
     * set when the value is present.
     */
    boolean required() default false;

    /**
     * Indicates that this property is a reference to another
     * configured inhabitant.
     *
     * See {@link Element#reference()} for more details of the semantics.
     *
     * <p>
     * When a reference property is a collection/array, then the key values
     * are separated by ',' with surrounding whitespaces ignored. That is,
     * it can be things like " foo , bar " (which would mean the same thing as
     * "foo,bar".)
     */
    boolean reference() default false;

    /**
     * Indicates that the variable expansion should be performed on this proeprty.
     *
     * See {@link Element#variableExpansion()} for more details.
     */
    boolean variableExpansion() default true;

    /** Specifies the default value of the attribute.
     * @return default value as String
     */
    String defaultValue() default "\u0000";

    /** Specifies the data type. It should be the fully qualified name of
     *  the class that identifies the real data type. For attributes that
     *  are of type defined by basic Java primitives (or wrappers), there is
     *  no need to specify this field. The default value is derived from
     *  method/field declaration.
     * @see {@link DataType}
     * @return String specifying the name of the data type for the values of this
     * attribute
     */
    Class dataType() default String.class;
}
