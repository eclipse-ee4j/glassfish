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

package com.sun.enterprise.tools.verifier.tests;

import com.sun.enterprise.tools.verifier.Result;
import org.glassfish.deployment.common.Descriptor;
import com.sun.enterprise.deployment.InjectionCapable;
import com.sun.enterprise.deployment.InjectionTarget;

import java.util.List;
import java.util.Set;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Method;

/**
 * The field or method where injection annotation is used may have any access 
 * qualifier (public , private , etc.) but must not be static or final.
 * This is the base class of all the InjectionAnnotation tests. 
 * 
 * @author Vikas Awasthi
 */
public abstract class InjectionTargetTest extends VerifierTest implements VerifierCheck {
// Currently only ejbs are checked for injection annotations. Other modules can 
// also use this class to test the assertion.
    protected abstract List<InjectionCapable> getInjectables(String className);
    protected abstract String getClassName();
    private Descriptor descriptor;
    Result result; 
    ComponentNameConstructor compName;
    
    public Result check(Descriptor descriptor) {
        this.descriptor = descriptor;
        result = getInitializedResult();
        compName = getVerifierContext().getComponentNameConstructor();
        ClassLoader cl = getVerifierContext().getClassLoader();
        List<InjectionCapable> injectables = getInjectables(getClassName());
        for (InjectionCapable injectionCapable : injectables) {
            Set<InjectionTarget> iTargets =  injectionCapable.getInjectionTargets();
            for (InjectionTarget target : iTargets) {
                try {
                    if(target.isFieldInjectable()) {
                        Class classObj = Class.forName(getClassName(), false, cl);
                        Field field = classObj.getDeclaredField(target.getFieldName());
                        testMethodModifiers(field.getModifiers(), "field", field);
                    }
                    if(target.isMethodInjectable()) {
                        Class classObj = Class.forName(getClassName(), false, cl);
                        Method method = getInjectedMethod(classObj, target.getMethodName());
                        if(method == null) continue;
                        testMethodModifiers(method.getModifiers(), "method", method);
                    }
                } catch (Exception e) {} //ignore as it will be caught in other tests
            }  
        }
        
        if(result.getStatus() != Result.FAILED) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                    (getClass().getName()+".passed",
                    "Valid injection method(s)."));
        }
        return result;
    }

    protected Descriptor getDescriptor() {
        return descriptor;
    }
    
    private void testMethodModifiers(int modifier, String targetType, Object fieldOrMethod) {
        if(Modifier.isStatic(modifier) ||
                Modifier.isFinal(modifier)) {
            addErrorDetails(result, compName);
            result.failed(smh.getLocalString
                    ("com.sun.enterprise.tools.verifier.tests.InjectionTargetTest.failed",
                    "Invalid annotation in {0} [ {1} ].",
                    new Object[] {targetType, fieldOrMethod}));
        }
    }
    
    private Method getInjectedMethod(Class classObj, String methodName) {
        for (Method method : classObj.getDeclaredMethods()) {
            if(method.getName().equals(methodName)) 
                return method;
        }
        return null;
    }
}
