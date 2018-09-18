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

package com.sun.enterprise.tools.verifier.tests.web;

import java.lang.reflect.*;
import java.util.logging.Level;

import com.sun.enterprise.tools.verifier.Result;

/** 
 * Filter class must implement a no arg constructor.
 * Verify that the Filter class implements a no arg constructor.
 * 
 * @author Jerome Dochez
 * @version 1.0
 */
public class FilterClassHasValidConstructor extends FilterClass implements WebCheck {
    /**
     * <p>
     * Run the verifier test against a declared individual filter class
     * </p>
     *
     * @param result is used to put the test results in
     * @param filterClass is the individual filter class object to test
     * @return true if the test pass
     */        
    protected boolean runIndividualFilterTest(Result result, Class filterClass) {
        
        boolean validConstructor = false;
        boolean foundIt = false;
        Class clazz = filterClass;
        
        if (filterClass == null) 
            return false;
        
        // walk up the class tree 
        do {
            Constructor[] constructors = filterClass.getDeclaredConstructors();
            for (int i = 0; i < constructors.length; i++) {
                logger.log(Level.FINE, getClass().getName() + ".debug",
                        new Object[] {constructors[i].toString()});
                Class[] parameters = constructors[i].getParameterTypes();
                if (parameters.length == 0) {
                    // we found it, let's look if it is public
                    if (Modifier.isPublic(constructors[i].getModifiers())) {
                        validConstructor = true;
                    } else {
                        validConstructor = false;
                    }
                    // we exit anyway...
                    foundIt = true;;
                }
            }
        } while ((!foundIt) && ((filterClass=filterClass.getSuperclass()) != null));
        
        if (validConstructor) {
            result.addGoodDetails(smh.getLocalString
                (getClass().getName() + ".passed",
                    "Listener class [ {0} ] implements a valid constructor.",
                    new Object[] {filterClass.getName()}));
        } else {
            result.addErrorDetails(smh.getLocalString
                (getClass().getName() + ".failed",
                 "Error: Listener class [ {0} ] does not implement a valid constructor.",
                 new Object[] {clazz.getName()}));
        }   
        return validConstructor;
    }
}
