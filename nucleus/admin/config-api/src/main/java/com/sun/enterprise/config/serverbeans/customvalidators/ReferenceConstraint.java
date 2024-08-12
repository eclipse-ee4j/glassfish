/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jvnet.hk2.config.ConfigBeanProxy;

/**
 * Annotated {@code ConfigBeanProxy} class contains at least one {@code String} field, which value must point to key
 * attribute of some other existing {@code ConfigBeanProxy} instance.<br/>
 * Use {@link ReferenceConstraint.RemoteKey} annotation on appropriate getters to define such fields.<br/>
 * This constraint is supported for {@code ConfigBeanProxy} only.
 *
 * @author Martin Mares
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Constraint(validatedBy = ReferenceValidator.class)
public @interface ReferenceConstraint {
    String message() default "Invalid reference in provided configuration.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * In GlassFish a lot of configurations are made in batch and its references could not be fulfilled during creation
     * process.
     */
    boolean skipDuringCreation();

    /**
     * This annotation gets set only on getter method and in combination with {@link ReferenceConstraint} annotation on the
     * class.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @Documented
    public @interface RemoteKey {
        String message() default "";

        /**
         * Type of {@code ConfigBeanProxy} where this remote key points to.
         */
        Class<? extends ConfigBeanProxy> type();
    }
}
