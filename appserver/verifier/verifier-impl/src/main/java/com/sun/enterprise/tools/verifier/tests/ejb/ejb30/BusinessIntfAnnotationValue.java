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

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

import javax.ejb.Local;
import javax.ejb.Remote;
import java.util.Set;

/**
 * It is an error if @Local or @Remote is specified both on the bean class and 
 * on the referenced interface and the values differ. (Expert Group discussions)
 * 
 * @author Vikas Awasthi
 */
public class BusinessIntfAnnotationValue extends EjbTest {

    private Result result;
    private ComponentNameConstructor compName;
    private Class<Remote> remoteAnn = Remote.class;
    private Class<Local> localAnn = Local.class;
    
    public Result check(EjbDescriptor descriptor) {
        result = getInitializedResult();
        compName = getVerifierContext().getComponentNameConstructor();

        testInterfaces(descriptor.getLocalBusinessClassNames(), remoteAnn);
        testInterfaces(descriptor.getRemoteBusinessClassNames(), localAnn);
    
        if(result.getStatus() != Result.FAILED) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                            (getClass().getName() + ".passed",
                            "Valid annotations used in business interface(s)."));
        }
        return result;
    }
    
    private void testInterfaces(Set<String> interfaces, Class annot) {
        // used in failure message
        Class cls = (annot.equals(localAnn))? remoteAnn : localAnn;

        for (String intf : interfaces) {
            try {
                Class intfCls = Class.forName(intf,
                                             false,
                                             getVerifierContext().getClassLoader());
                if(intfCls.getAnnotation(annot)!=null) {
                    addErrorDetails(result, compName);
                    result.failed(smh.getLocalString
                                 (getClass().getName() + ".failed",
                                 "{0} annotation is used in {1} interface [ {2} ].", 
                                 new Object[]{annot.getSimpleName(), 
                                             cls.getSimpleName(), 
                                             intfCls.getName()}));
                }
            } catch (ClassNotFoundException e) {
             //ignore as it will be caught in other tests
            }
        }
    }
}
