/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.apf.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.glassfish.apf.ComponentInfo;
import org.glassfish.apf.factory.Factory;


/**
 * This class represents the view of a class from annotation.
 *
 * @author Shing Wai Chan
 */
public class ComponentDefinition implements ComponentInfo {

    private final Class<?> clazz;
    private final List<Constructor<?>> constructors = new ArrayList<>();
    private final List<Class<?>> classes = new ArrayList<>();
    private final List<Field> fields = new ArrayList<>();
    private final Map<MethodKey, Method> methodMap = new HashMap<>();

    public ComponentDefinition(Class<?> clazz) {
        this.clazz = clazz;
        constructClassList();
        initializeConstructors();
        initializeFields();
        initializeMethods();
    }


    @Override
    public Field[] getFields() {
        return fields.toArray(new Field[fields.size()]);
    }


    @Override
    public Method[] getMethods() {
        return methodMap.values().toArray(new Method[methodMap.size()]);
    }


    @Override
    public Constructor<?>[] getConstructors() {
        return constructors.toArray(new Constructor[constructors.size()]);
    }


    private void constructClassList() {
        // check whether this class is in the skip list
        if (!Factory.isSkipAnnotationProcessing(clazz)) {
            classes.add(clazz);
        }
        Class<?> parent = clazz;
        while ((parent = parent.getSuperclass()) != null) {
            // always check whether this class is in the class list
            // for skipping annotation processing
            if (!Factory.isSkipAnnotationProcessing(parent)) {
                classes.add(0, parent);
            }
        }
    }


    /**
     * In P.148 of "The Java Langugage Specification 2/e",
     * Constructors, static initializers, and instance initializers are not
     * members and therefore not inherited.
     */
    private void initializeConstructors() {
        for (Class<?> cl : classes) {
            for (Constructor<?> constr : cl.getConstructors()) {
                constructors.add(constr);
            }
        }
    }


    private void initializeFields() {
        for (Class<?> cl : classes) {
            for (Field f : cl.getDeclaredFields()) {
                fields.add(f);
            }
        }
    }


    private void initializeMethods() {
        for (Class<?> cl : classes) {
            for (Method method : cl.getDeclaredMethods()) {
                if (!method.isBridge()) {
                    methodMap.put(new MethodKey(method), method);
                }
            }
        }
    }

    private static class MethodKey {
        private final Method m;
        private final Package classPackage;
        private final String className;
        private final int hashCode;

        private MethodKey(Method m) {
            this.m = m;
            // store className and classPackage as getters are native
            this.className = m.getDeclaringClass().getName();
            this.classPackage = m.getDeclaringClass().getPackage();
            this.hashCode = Objects.hash(m.getName(), className);
        }


        @Override
        public int hashCode() {
            return hashCode;
        }


        /**
         * This equals method is defined in terms of inheritance overriding.
         * We depends on java compiler to rule out irrelvant cases here.
         *
         * @return true for overriding and false otherwise
         */
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof MethodKey)) {
                return false;
            }

            MethodKey mk2 = (MethodKey) o;
            Method m2 = mk2.m;
            if (m.getName().equals(m2.getName()) && Arrays.equals(m.getParameterTypes(), m2.getParameterTypes())) {
                int modifiers = m.getModifiers();
                int modifiers2 = m2.getModifiers();
                boolean isPackageProtected2 = !Modifier.isPublic(modifiers2) && !Modifier.isProtected(modifiers2)
                    && !Modifier.isPrivate(modifiers2);
                boolean isSamePackage = (classPackage == null && mk2.classPackage == null) || (classPackage != null
                    && mk2.classPackage != null && classPackage.getName().equals(mk2.classPackage.getName()));
                if (Modifier.isPrivate(modifiers)) {
                    // need exact match
                    return Modifier.isPrivate(modifiers2) && isSamePackage && className.equals(mk2.className);
                }
                return Modifier.isPublic(modifiers2) || Modifier.isProtected(modifiers2)
                    || isPackageProtected2 && isSamePackage;
            }

            return false;
        }
    }
}
