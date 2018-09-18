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

package com.sun.enterprise.tools.verifier.tests.ejb.ejb30;

import java.lang.reflect.Method;
import java.util.Set;

import org.glassfish.ejb.deployment.descriptor.EjbInitInfo;
import org.glassfish.ejb.deployment.descriptor.EjbSessionDescriptor;

import com.sun.enterprise.tools.verifier.Result;

/**
 * The Init annotation is used to specify the correspondence of a method on the 
 * bean class with a createMETHOD method for an adapted EJB 2.1 EJBHome and/or 
 * EJBLocalHome client view. 
 * The result type of such an Init method is required to be void.
 * 
 * @author Vikas Awasthi
 */
public class InitMethodReturnType extends SessionBeanTest {

    public Result check(EjbSessionDescriptor descriptor) {
        Set<EjbInitInfo> initMethods = descriptor.getInitMethods();
        for (EjbInitInfo initInfo : initMethods) {
            Method method = initInfo.getBeanMethod().getMethod(descriptor);
            if(!method.getReturnType().getName().equals("void")) {
                addErrorDetails(result, compName);
                result.failed(smh.getLocalString
                        (getClass().getName()+".failed",
                        "Wrong init method [ {0} ].",
                        new Object[] {method}));
            }
        }
        
        if(result.getStatus() != Result.FAILED) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                    (getClass().getName()+".passed",
                    "Valid init method(s)."));
        }
        return result;
    }
}
