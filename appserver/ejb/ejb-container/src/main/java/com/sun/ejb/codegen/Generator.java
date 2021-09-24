/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package com.sun.ejb.codegen;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


/**
 * The base class for all code generators.
 */
public abstract class Generator implements ClassGeneratorFactory {

    /**
     * @return the package name or null.
     */
    public static String getPackageName(String fullClassName) {
        int dot = fullClassName.lastIndexOf('.');
        return dot == -1 ? null : fullClassName.substring(0, dot);
    }

    /**
     * @return simple class name (including wrapper class and dollar sign if it is internal class)
     */
    public static String getBaseName(String fullClassName) {
        int dot = fullClassName.lastIndexOf('.');
        return dot == -1 ? fullClassName : fullClassName.substring(dot + 1);
    }


    /**
     * Remove duplicates from method array.
     * <p>
     * Duplicates will arise if a class/intf and super-class/intf
     * define methods with the same signature. Potentially the
     * throws clauses of the methods may be different (note Java
     * requires that the superclass/intf method have a superset of the
     * exceptions in the derived method).
     *
     * @param methods
     * @return methods which can be generated in an interface
     */
    protected Method[] removeRedundantMethods(Method[] methods) {
        final List<Method> nodups = new ArrayList<>();
        for (Method method : methods) {
            boolean duplicationDetected = false;
            final List<Method> previousResult = new ArrayList<>(nodups);
            for (Method alreadyProcessed : previousResult) {
                // m1 and m2 are duplicates if they have the same signature
                // (name and same parameters).
                if (!method.getName().equals(alreadyProcessed.getName())) {
                    continue;
                }
                if (!haveSameParams(method, alreadyProcessed)) {
                    continue;
                }
                duplicationDetected = true;
                // Select which of the duplicate methods to generate
                // code for: choose the one that is lower in the
                // inheritance hierarchy: this ensures that the generated
                // method will compile.
                if (alreadyProcessed.getDeclaringClass().isAssignableFrom(method.getDeclaringClass())) {
                    // alreadyProcessedMethod is a superclass/intf of method,
                    // so replace it with more concrete method
                    nodups.remove(alreadyProcessed);
                    nodups.add(method);
                }
                break;
            }

            if (!duplicationDetected) {
                nodups.add(method);
            }
        }
        return nodups.toArray(new Method[nodups.size()]);
    }


    private boolean haveSameParams(final Method method1, final Method method2) {
        Class<?>[] m1parms = method1.getParameterTypes();
        Class<?>[] m2parms = method2.getParameterTypes();
        if (m1parms.length != m2parms.length) {
            return false;
        }
        for (int i = 0; i < m2parms.length; i++) {
            if (m1parms[i] != m2parms[i]) {
                return false;
            }
        }
        return true;
    }
}
