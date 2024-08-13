/*
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.config.serverbeans.customvalidators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.HashSet;

/**
 * Validation logic for NotTargetKeyword constraint
 *
 * @author Joe Di Pol
 */
public class NotTargetKeywordValidator implements ConstraintValidator<NotTargetKeyword, String> {

    static private final HashSet<String> keywords = new HashSet<String>(5);
    static {
        // Add keywords here
        keywords.add("domain");
    }

    @Override
    public void initialize(NotTargetKeyword constraintAnnotation) {
        //no initialization needed
    }

    @Override
    public boolean isValid(String name, ConstraintValidatorContext context) {
        if (name == null)
            return true;
        return !keywords.contains(name);
    }
}
