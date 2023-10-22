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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

/**
 * Implementation of this abstract class are handling injection resolution
 * for a particular injection annotation {@see Inject}
 *
 * Injection targets are identified by the generic parameter and the constructor
 * of this class. Potential injection targets are fields and methods of the
 * injected type.
 *
 * @param <U> U is the annotation used to identify the injection targets.
 *
 * @author Jerome Dochez
 */
public abstract class InjectionResolver<U extends Annotation> implements InjectionResolverQuery {

    public final Class<U> type;

    /**
     * Construct a resolver with a particular injection type
     * @param type the injection annotation type
     */
    public InjectionResolver(Class<U> type) {
        this.type = type;
    }

    /**
     * Returns the setter method responsible for setting the resource identified by the
     * passed annotation on the passed annotated method.
     *
     * This is useful when the annotation is specified on the getter for instance (due
     * to external specification requirements for instance) while the setter should be used if
     * values must be set using this injection resolver.
     *
     * By default, the setter method is the annotated method.
     *
     * @param annotated is the annotated {@link java.lang.reflect.Method}
     * @param annotation the annotation on the method
     * @return the setter method to use for injecting the annotation identified resource
     */
    public Method getSetterMethod(Method annotated, U annotation) {
        return annotated;
    }

    /**
     * Returns true if the resolution of this injection identified by the
     * passed annotation instance is optional
     * @param annotated is the annotated java element {@link java.lang.reflect.Method}
     * or {@link java.lang.reflect.Field}
     * @param annotation the injection metadata
     * @return true if the {@see getValue()} can return null without generating a
     * faulty injection operation
     */
    public boolean isOptional(AnnotatedElement annotated, U annotation) {
        return false;
    }

}
