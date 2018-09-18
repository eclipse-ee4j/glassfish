/*
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

/*
 */
package com.sun.ejb.containers.interceptors;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;


/**
 */
public class InterceptorUtil {

    private static Map<Class, Set<Class>> compatiblePrimitiveWrapper
         = new HashMap<Class, Set<Class>>();

     static {

         Set<Class> smallerPrimitiveWrappers = null;

         smallerPrimitiveWrappers = new HashSet<Class>();
         smallerPrimitiveWrappers.add(Byte.class);
         compatiblePrimitiveWrapper.put(byte.class, smallerPrimitiveWrappers);

         smallerPrimitiveWrappers = new HashSet<Class>();
         smallerPrimitiveWrappers.add(Boolean.class);
         compatiblePrimitiveWrapper.put(boolean.class, smallerPrimitiveWrappers);

         smallerPrimitiveWrappers = new HashSet<Class>();
         smallerPrimitiveWrappers.add(Character.class);
         compatiblePrimitiveWrapper.put(char.class, smallerPrimitiveWrappers);

         smallerPrimitiveWrappers = new HashSet<Class>();
         smallerPrimitiveWrappers.add(Byte.class);
         smallerPrimitiveWrappers.add(Short.class);
         smallerPrimitiveWrappers.add(Integer.class);
         smallerPrimitiveWrappers.add(Float.class);
         smallerPrimitiveWrappers.add(Double.class);
         compatiblePrimitiveWrapper.put(double.class, smallerPrimitiveWrappers);

         smallerPrimitiveWrappers = new HashSet<Class>();
         smallerPrimitiveWrappers.add(Byte.class);
         smallerPrimitiveWrappers.add(Short.class);
         smallerPrimitiveWrappers.add(Integer.class);
         smallerPrimitiveWrappers.add(Float.class);
         compatiblePrimitiveWrapper.put(float.class, smallerPrimitiveWrappers);

         smallerPrimitiveWrappers = new HashSet<Class>();
         smallerPrimitiveWrappers.add(Byte.class);
         smallerPrimitiveWrappers.add(Short.class);
         smallerPrimitiveWrappers.add(Integer.class);
         compatiblePrimitiveWrapper.put(int.class, smallerPrimitiveWrappers);

         smallerPrimitiveWrappers = new HashSet<Class>();
         smallerPrimitiveWrappers.add(Byte.class);
         smallerPrimitiveWrappers.add(Short.class);
         smallerPrimitiveWrappers.add(Integer.class);
         smallerPrimitiveWrappers.add(Long.class);
         compatiblePrimitiveWrapper.put(long.class, smallerPrimitiveWrappers);

         smallerPrimitiveWrappers = new HashSet<Class>();
         smallerPrimitiveWrappers.add(Byte.class);
         smallerPrimitiveWrappers.add(Short.class);
         compatiblePrimitiveWrapper.put(short.class, smallerPrimitiveWrappers);
     }

    public static boolean hasCompatiblePrimitiveWrapper(Class type, Class typeTo) {
        Set<Class> compatibles = compatiblePrimitiveWrapper.get(type);
        return compatibles.contains(typeTo);
    }

    public static void checkSetParameters(Object[] params, Method method) {

        if( method != null) {

            Class[] paramTypes = method.getParameterTypes();
            if ((params == null) && (paramTypes.length != 0)) {
                throw new IllegalArgumentException("Wrong number of parameters for "
                        + " method: " + method);
            }
            if (params!= null && paramTypes.length != params.length) {
                throw new IllegalArgumentException("Wrong number of parameters for "
                        + " method: " + method);
            }
            int index = 0 ;
            for (Class type : paramTypes) {
                if (params[index] == null) {
                    if (type.isPrimitive()) {
                        throw new IllegalArgumentException("Parameter type mismatch for method "
                                + method.getName() + ".  Attempt to set a null value for Arg["
                            + index + "]. Expected a value of type: " + type.getName());
                    }
                } else if (type.isPrimitive()) {
                    Set<Class> compatibles = compatiblePrimitiveWrapper.get(type);
                    if (! compatibles.contains(params[index].getClass())) {
                        throw new IllegalArgumentException("Parameter type mismatch for method "
                                + method.getName() + ".  Arg["
                            + index + "] type: " + params[index].getClass().getName()
                            + " is not compatible with the expected type: " + type.getName());
                    }
                } else if (! type.isAssignableFrom(params[index].getClass())) {
                    throw new IllegalArgumentException("Parameter type mismatch for method "
                            + method.getName() + ".  Arg["
                        + index + "] type: " + params[index].getClass().getName()
                        + " does not match the expected type: " + type.getName());
                }
                index++;
            }
        } else {
            throw new IllegalStateException("Internal Error: Got null method");
        }

    }



}
