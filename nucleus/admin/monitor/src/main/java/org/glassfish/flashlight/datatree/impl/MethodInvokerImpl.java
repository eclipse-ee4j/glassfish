/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight.datatree.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.glassfish.flashlight.datatree.MethodInvoker;

/**
 *
 * @author Harpreet Singh
 */
public class MethodInvokerImpl extends AbstractTreeNode implements MethodInvoker {
    Method method;
    Object methodInstance;

    public void setMethod(Method m) {
        method = m;
    }

    public Method getMethod() {
        return method;
    }

    public void setInstance(Object i) {
        methodInstance = i;
    }

    public Object getInstance() {

        return methodInstance;
    }

    @Override
    // TBD Put Logger calls.
    public Object getValue() {
        Object retValue = null;
        try {
            if (method == null) {
                throw new RuntimeException("Flashlight:MethodInvoker: method, " + "is null - cannot be null.");
            }
            if (methodInstance == null)
                throw new RuntimeException("Flashlight:MethodInvoker: object, " + " instance is null - cannot be null.");

            if (super.isEnabled())
                retValue = method.invoke(methodInstance, null);
        } catch (IllegalAccessException ex) {

            // Logger.getLogger(MethodInvokerImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            // Logger.getLogger(MethodInvokerImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            // Logger.getLogger(MethodInvokerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return retValue;
    }

}
