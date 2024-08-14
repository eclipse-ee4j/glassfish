/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Param is a parameter to a command. This annotation can be placed on a field or setter method to identify the
 * parameters of a command and have those parameters injected by the system before the command is executed.
 *
 * The system will check that all non optional parameters are satisfied before invoking the command.
 *
 * @author Jerome Dochez
 */
@Retention(RUNTIME)
@Target({ METHOD, FIELD })
public @interface Param {

    /**
     * Returns the name of the parameter as it has be specified by the client when invoking the command. By default the name
     * is deducted from the name of the annotated element. If the annotated element is a field, it is the filed name. If the
     * annotated element is a method, it is the JavaBeans property name from the setter method name
     *
     * @return the parameter name.
     */
    public String name() default "";

    /**
     * Returns a list of comma separated acceptable values for this parameter. The system will check that one of the value
     * is used before invoking the command.
     *
     * @return the list of comma separated acceptable values
     */
    public String acceptableValues() default "";

    /**
     * Returns true if the parameter is optional to the successful invocation of the command
     *
     * @return true if the parameter is optional
     */
    public boolean optional() default false;

    /**
     * Returns the short name associated with the parameter so that the user can specify -p as well as -password when
     * invoking the command.
     *
     * @return the parameter short name
     */
    public String shortName() default "";

    /**
     * Returns true if this is the primary parameter for the command which mean that the client does not have to pass the
     * parameter name but just the value to the command.
     *
     * @return true if this is the primary command parameter.
     */
    public boolean primary() default false;

    /**
     * Returns the default value associated with the parameter so that the user can specify
     *
     * @return the parameter default value
     */
    public String defaultValue() default "";

    /**
     * Returns a class that calculates the default value associated with the parameter.
     *
     * @return a parameter default value calculator
     */
    public Class<? extends ParamDefaultCalculator> defaultCalculator() default ParamDefaultCalculator.class;

    /**
     * Returns true if the parameter is a password
     *
     * @return true if the parameter is password
     */
    public boolean password() default false;

    /**
     * Returns the character used to separate items in a list. Applies to parameters of type List, Properties, and String[].
     * The default separator is comma.
     *
     * @return the separator character
     */
    public char separator() default ',';

    /**
     * Returns true if multiple instances of the parameter are allowed.
     *
     * @return true if multiple instances are allowed
     */
    public boolean multiple() default false;

    /**
     * Returns true if this parameter is obsolete. Obsolete parameters produce warnings when used in asadmin, are ignored,
     * and are not included in the command usage.
     *
     * @return true if the parameter is obsolete
     */
    public boolean obsolete() default false;

    /**
     * Returns an alias for the option. This supports renaming options and supporting both the old name and the new name.
     *
     * @return the parameter alias
     */
    public String alias() default "";
}
