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
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Locale;

/**
 * Exception thrown by the injection manager when a dependency is not satisfied when
 * performing injection.
 *
 * @author Jerome Dochez
 */
public class UnsatisfiedDependencyException extends ConfigurationException {

    private static final long serialVersionUID = 1L;
    final AnnotatedElement member;

    /**
     * @deprecated
     */
    @Deprecated
    public UnsatisfiedDependencyException(AnnotatedElement target) {
        this(target, null, null);
    }


    public UnsatisfiedDependencyException(AnnotatedElement target, Annotation inject) {
        this(target, inject, null);
    }


    public UnsatisfiedDependencyException(AnnotatedElement target, Annotation inject, Throwable cause) {
        super(injection_failed_msg(target, inject, cause), cause);
        this.member = target;
    }


    public UnsatisfiedDependencyException(Type target, Class<?> targetClass, Annotation inject, Throwable cause) {
        super(injection_failed_msg(target, inject, null), inject, cause);
        this.member = targetClass;
    }


    static String injection_failed_msg(Object t, Annotation inject, Throwable cause) {
        String name = null;
        // name = (null == name || name.isEmpty()) ? null : name;
        String msg;
        if (Field.class.isInstance(t)) {
            Field target = Field.class.cast(t);
            msg = "injection failed on " + target.getDeclaringClass().getName() + "." + target.getName() + " with "
                + target.getGenericType() + "";
        } else {
            msg = "injection failed on " + t + "";
        }
        return msg;
    }


    public boolean isField() {
        return member instanceof Field;
    }


    public boolean isMethod() {
        return member instanceof Method;
    }


    public String getUnsatisfiedName() {
        String name = (member instanceof Member) ? ((Member) member).getName() : member.toString();
        if (isMethod()) {
            return name.substring(3).toLowerCase(Locale.ENGLISH);
        }
        return name;
    }


    public AnnotatedElement getUnsatisfiedElement() {
        try {
            return AnnotatedElement.class.cast(member);
        } catch (ClassCastException e) {
            return null;
        }
    }


    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        AnnotatedElement annotated = getUnsatisfiedElement();
        if (annotated != null) {
            return annotated.getAnnotation(annotationType);
        } else {
            return null;
        }
    }
}
