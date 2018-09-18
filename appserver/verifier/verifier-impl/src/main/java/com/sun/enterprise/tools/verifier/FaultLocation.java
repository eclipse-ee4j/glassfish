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

package com.sun.enterprise.tools.verifier;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * This class represents the fault location of the failed verifier assertions.
 * It can be used by IDE's to open the faulty source file in the editor.
 * 
 * The location object is initialized as part of Result initialization and so it
 * is available in every assertion. Just like every assertion has to populate
 * the result object, it will have to populate this location. 
 * For now, few assertions in package 'tests.ejb.ejb30' are modified to provide 
 * the location information.
 * 
 * @author Vikas Awasthi
 */
public class FaultLocation {

    private Class faultyClass;
    private Method faultyMethod;
    private Field faultyField;
    private String faultyClassName;
    private String faultyMethodName;
    private String[] faultyMethodParams;

    public Class getFaultyClass() {
        return faultyClass;
    }

    public void setFaultyClass(Class faultyClass) {
        this.faultyClass = faultyClass;
    }

    public Method getFaultyMethod() {
        return faultyMethod;
    }

    public void setFaultyMethod(Method faultyMethod) {
        this.faultyMethod = faultyMethod;
    }

    public String getFaultyClassName() {
        if(faultyClassName==null && faultyClass != null)
            faultyClassName = faultyClass.getName();
        return faultyClassName;
    }

    public void setFaultyClassName(String faultyClassName) {
        this.faultyClassName = faultyClassName;
    }

    public String getFaultyMethodName() {
        if(faultyMethodName==null && faultyMethod != null)
            faultyMethodName = faultyMethod.getName();
        return faultyMethodName;
    }

    public void setFaultyMethodName(String faultyMethodName) {
        this.faultyMethodName = faultyMethodName;
    }

    public Field getFaultyField() {
        return faultyField;
    }

    public void setFaultyField(Field faultyField) {
        this.faultyField = faultyField;
    }

    public String[] getFaultyMethodParams() {
        if(faultyMethodParams==null && faultyMethod != null) {
            ArrayList<String> l = new ArrayList<String>();
            for (Class<?> aClass : faultyMethod.getParameterTypes()) 
                l.add(aClass.getName());
            faultyMethodParams = l.toArray(new String[]{});
        }
        return faultyMethodParams;
    }
    
    public void setFaultyClassAndMethod(Method faultyMethod) {
        this.faultyClass = faultyMethod.getDeclaringClass();
        this.faultyMethod = faultyMethod;
    }

    public void setFaultyClassAndField(Field faultyField) {
        this.faultyField = faultyField;
        this.faultyClass = faultyField.getDeclaringClass();
    }
}
