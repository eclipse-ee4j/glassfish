/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.grizzly.config.dom;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Constraint validates the data is within the range of min to max or ${...} or null.
 *
 * @author Shing Wai Chan
 */
@Retention(RUNTIME)
@Target({FIELD, METHOD})
@Documented
@Constraint(validatedBy = RangeValidator.class)
public @interface Range {
    int min() default 0;
    int max() default 0;

    String message() default "must be between {min} and {max} or property substitution (a string starting with \"${\" and ending with \"}\"";

    Class<? extends Payload>[] payload() default {};

    Class<?>[] groups() default {};
}
