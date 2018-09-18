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

package com.sun.enterprise.tools.verifier.tests.ejb;

import java.util.*;
import java.lang.reflect.*;

/** 
 * Utility package for dealing with method descriptors from <method-permission>
 * element and <container-transaction> elements
 */
public class MethodUtils { 

    /** 
     * Add method name to vector
     *
     * @param v Vector to be added to
     * @param methods array of methods to be added to vector 
     *   
     */
    public static void addMethodNamesToVector(Vector<String> v, Method[] methods) {
        for (int i=0; i< methods.length; i++) {
            // add method name to vector
            v.addElement(methods[i].getName());
        }
    }
 
    /** 
     * Determine is method parameters are equal.
     *
     * @param v Vector to be added to
     * @param hMethods array of home interface methods to be added to vector 
     * @param rMethods array of remote interface methods to be added to vector 
     *   
     */
    public static void addMethodNamesToVector(Vector<String> v, Method[] hMethods, Method[] rMethods) {
        // add method names to vector for both home and remote interfaces
        addMethodNamesToVector(v,hMethods);
        addMethodNamesToVector(v,rMethods);
    }
 
   
    /** 
     * Determine is method parameters are equal.
     *
     * @param s1 array of parameters for method 
     * @param s2 array of parameters for method 
     *   
     * @return <code>boolean</code> the results for this parameter equality test
     */
    public static boolean stringArrayEquals(String[] s1, String[] s2) {
        if (s1 == null && s2 == null) {
            return true;
        }
        if (s1 == null && s2 != null) {
            return false;
        }
        if (s2 == null && s1 != null) {
            return false;
        }
        if (s1.length == s2.length) {
            for (int i = 0; i < s1.length; i++) {
                if (!s1[i].equals(s2[i])) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }
    
    /** returns true if method names, return types and parameters match. Otherwise
     * it returns false. */
    public static boolean methodEquals(Method classMethod, Method intfMethod) {
        return classMethod.getName().equals(intfMethod.getName()) &&
                intfMethod.getReturnType().isAssignableFrom(classMethod.getReturnType()) &&
                Arrays.equals(classMethod.getParameterTypes(), intfMethod.getParameterTypes());
    }

}
