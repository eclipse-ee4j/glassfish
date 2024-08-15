/*
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

package org.glassfish.admin.rest.composite.metadata;

import jakarta.inject.Qualifier;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author jdlee
 */
@Qualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Default {
    // Revert to the following once the JDK issue on the Hudson nodes is resolved
    // Class<? extends DefaultsGenerator> generator() default DefaultsGenerator.class;
    // or
    // Class<? extends DefaultsGenerator> generator() default NoopDefaultsGenerator.class;
    Class<?> generator() default Void.class;

    boolean useContext() default false;

    String value() default "";

    static class NoopDefaultsGenerator implements DefaultsGenerator {
        @Override
        public Object getDefaultValue(String propertyName) {
            return null;
        }
    }
}
