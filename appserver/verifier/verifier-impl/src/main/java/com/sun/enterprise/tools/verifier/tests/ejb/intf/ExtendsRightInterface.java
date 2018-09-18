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

package com.sun.enterprise.tools.verifier.tests.ejb.intf;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbSessionDescriptor;

/**
 * Remote interfaces extend the EJBObject interface test. Local interfaces extend 
 * the EJBLocalObject interface test. 
 * All enterprise beans remote interfaces must extend the EJBObject interface 
 * and/or local interfaces must extend the EJBLocalObject interface.
 * 
 * @author Sheetal Vartak
 */
abstract public class ExtendsRightInterface extends EjbTest implements EjbCheck { 
    /**
     * Following 3 methods are used to determine whether this method is being called by 
     * local/remote interface.
     */
    abstract protected String getInterfaceName(EjbDescriptor descriptor);
    abstract protected String getSuperInterface();
    abstract protected String getInterfaceType();
    
    /** 
     * local interfaces extend the EJBLocalObject interface and remote interfaces 
     * extend the EJBObject interface test.  
     * All enterprise beans remote interfaces must extend the EJBObject interface 
     * and/or local interfaces must extend the EJBLocalObject interface.
     * 
     * @param descriptor the Enterprise Java Bean deployment descriptor
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {
        
        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        String str = null;
        
        if (!(descriptor instanceof EjbSessionDescriptor) &&
                !(descriptor instanceof EjbEntityDescriptor)) {
            addNaDetails(result, compName);
            result.notApplicable(smh.getLocalString
                    ("com.sun.enterprise.tools.verifier.tests.ejb.homeintf.HomeMethodTest.notApplicable1",
                    "Test apply only to session or entity beans."));
            return result;
        }
        
        if(getInterfaceName(descriptor) == null || "".equals(getInterfaceName(descriptor))) {
            addNaDetails(result, compName);
            result.notApplicable(smh.getLocalString
                    ("com.sun.enterprise.tools.verifier.tests.ejb.intf.InterfaceTest.notApplicable",
                    "Not Applicable because, EJB [ {0} ] does not have {1} Interface.",
                    new Object[] {descriptor.getEjbClassName(), getInterfaceType()}));
            return result;
        }
        
        try {
            ClassLoader jcl = getVerifierContext().getClassLoader();	   
            Class c = Class.forName(getClassName(descriptor), false, jcl);
            str = getSuperInterface();
            
            if (isImplementorOf(c, str)) {
                addGoodDetails(result, compName);	
                result.passed(smh.getLocalString
                        (getClass().getName() + ".passed",
                        "[ {0} ] " + getInterfaceType() +" interface properly extends the" + str + " interface.",
                        new Object[] {getClassName(descriptor)}));
            } else {
                addErrorDetails(result, compName);
                result.failed(smh.getLocalString
                        (getClass().getName() + ".failed",
                        "Error: [ {0} ] does not properly extend the EJBObject interface. "+
                        " All enterprise bean" + getInterfaceType() + " interfaces must extend the" + str + "  interface."+
                        " [ {1} ] is not a valid "+ getInterfaceType() + "interface within bean [ {2} ]",
                        new Object[] {getClassName(descriptor),getClassName(descriptor),descriptor.getName()}));
            }
        } catch (ClassNotFoundException e) {
            Verifier.debug(e);
            addErrorDetails(result, compName);
            result.failed(smh.getLocalString
                    (getClass().getName() + ".failedException",
                    "Error: [ {0} ] class not found.",
                    new Object[] {getClassName(descriptor)}));
        }
        return result;
    }
    //get the interface class name
    private String getClassName(EjbDescriptor descriptor) {
        return getInterfaceName(descriptor);
    }
}
