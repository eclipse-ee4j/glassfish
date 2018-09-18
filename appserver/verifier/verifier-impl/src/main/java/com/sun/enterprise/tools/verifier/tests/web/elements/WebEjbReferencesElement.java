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

package com.sun.enterprise.tools.verifier.tests.web.elements;

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;
import com.sun.enterprise.tools.verifier.tests.web.*;
import com.sun.enterprise.tools.verifier.tests.web.WebCheck;
import com.sun.enterprise.tools.verifier.tests.web.WebTest;

import java.io.*;
import java.lang.ClassLoader;
import java.util.*;
import java.util.jar.*;
import java.util.zip.*;


/** 
 * The Bean Provider must declare all enterprise bean's references to the
 * homes of other enterprise beans as specified in section 14.3.2 of the 
 * Moscone spec.  Check for one within the same jar file, can't check 
 * outside of jar file.  Load/locate & check other bean's home/remote/bean,
 * ensure they match with what the linking bean says they should be; check
 * for pair of referencing and referenced beans exist.
 */
public class WebEjbReferencesElement extends WebTest implements WebCheck { 
    boolean oneFailed=false;
    /** 
     *
     * @param descriptor the Web deployment descriptor
     *
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(WebBundleDescriptor descriptor) {

        Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
	String f=descriptor.getModuleDescriptor().getArchiveUri();
	loadWarFile(descriptor);
        result.notApplicable(smh.getLocalString
           ("tests.componentNameConstructor",
            "For [ {0} ]",
            new Object[] {compName.toString()}));
        result.addNaDetails(smh.getLocalString
                              (getClass().getName() + ".notApplicable",
                               "There is no ejb-ref inside [ {0} ].",
                               new Object[] {compName}));
        result.addGoodDetails(smh.getLocalString
                           ("tests.componentNameConstructor",
                            "For [ {0} ]",
                            new Object[] {compName}));	
        result.addErrorDetails(smh.getLocalString
               ("tests.componentNameConstructor",
                "For [ {0} ]",
                new Object[] {compName.toString()}));


        Set references = descriptor.getEjbReferenceDescriptors();
        Iterator iterator = references.iterator();
        while (iterator.hasNext()) {
            EjbReferenceDescriptor ejbReference = (EjbReferenceDescriptor) iterator.next();
            checkInterface(result, ejbReference, ejbReference.getEjbHomeInterface(), f);
            checkInterface(result, ejbReference, ejbReference.getEjbInterface(), f);
        }
        return result;
    }

    private void checkInterface(Result result, EjbReferenceDescriptor ejbRef, String intf, String f){
        Class cl = loadClass(result, intf);
        if(cl==null){
            oneFailed=true;
            result.failed(smh.getLocalString
                          (getClass().getName() + ".failed",
                           "Error: For ejb-ref element [ {0} ] the home/component interface class [ {1} ] is not loadable within [ {2} ].",
                           new Object[] {ejbRef.getName(), intf, f}));
        }else if(!oneFailed) {
            result.passed(smh.getLocalString
                          (getClass().getName() + ".passed",
                           "For ejb-ref element [ {0} ] the home/component interface class [ {1} ] is loadable within [ {2} ].",
                           new Object[] {ejbRef.getName(), intf, f}));
        }
    }
}    


