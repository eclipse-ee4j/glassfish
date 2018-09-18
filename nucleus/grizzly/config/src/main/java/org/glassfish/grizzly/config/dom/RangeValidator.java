/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * ConstraintValidator for validating the Range within min to max or ${...} or null.
 *
 * @author Shing Wai Chan
 */
public class RangeValidator implements ConstraintValidator<Range, String> {
    private int min;
    private int max;

    public void initialize(final Range range) {
        min = range.min();
        max = range.max();
    }

    public boolean isValid(final String s, final ConstraintValidatorContext constraintValidatorContext) {
        if (s == null) {
            return true;
        }

        try {
            int value = Integer.parseInt(s);
            return (value >= min && value <= max);
        } catch (NumberFormatException e) {
            return s.charAt(0) == '$'
                    && s.charAt(1) == '{' && s.charAt(s.length() - 1) == '}';
        }
    }
}
