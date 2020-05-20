/*
 * Copyright (c) 2009, 2020 Oracle and/or its affiliates. All rights reserved.
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

/**
 * Implementation for the user-defined constraint annotation @JavaClassName 
 * @author Nandini Ektare
 */
public class JavaClassNameValidator
implements ConstraintValidator<JavaClassName, String> {

    public void initialize(final JavaClassName fqcn) {}

    public boolean isValid(final String fullyQualifiedClassName,                
        final ConstraintValidatorContext constraintValidatorContext) {

        try {
            return isValidPackageName(fullyQualifiedClassName);
        } catch (Exception e) {
            return false;
        }
    }

    /** Is the given string a valid package name? */
    private boolean isValidPackageName(String fqcn) {
        int index;

        if (fqcn.indexOf('.') == -1) {
            return isValidClassName(fqcn);
        }

        while ((index = fqcn.indexOf('.')) != -1) {
            if (!isValidClassName(fqcn.substring(0, index))) {
                return false;
            }
            fqcn = fqcn.substring(index+1);
        }
        return isValidClassName(fqcn);
    }

    private boolean isValidClassName(String className) {
        boolean valid = true;
        for(int i=0;i<className.length();i++) {
            if(i == 0) {
                if(!Character.isJavaIdentifierStart(className.charAt(i)))
                    valid = false;
            }
            if(!Character.isJavaIdentifierPart(className.charAt(i)))
                valid = false;
        }
        return valid;
    }
}
