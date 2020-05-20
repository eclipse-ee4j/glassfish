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

package com.sun.enterprise.tools.verifier.tests.ejb.intf.remoteintf;

import com.sun.enterprise.deployment.EjbSessionDescriptor;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import com.sun.enterprise.tools.verifier.tests.ejb.RmiIIOPUtils;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;

import java.util.logging.Level;

/**
 * Remote interface/business methods test.  
 * Verify the following:
 * 
 *   The remote interface is allowed to have superinterfaces. Use of interface 
 *   inheritance is subject to the RMI-IIOP rules for the definition of remote 
 *   interfaces. 
 * 
 */
public class RemoteInterfaceSuperInterface extends EjbTest implements EjbCheck { 
    
    /**
     * Remote interface/business methods test.
     * Verify the following:
     *
     *   The remote interface is allowed to have superinterfaces. Use of interface
     *   inheritance is subject to the RMI-IIOP rules for the definition of remote
     *   interfaces.
     *
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {
        
        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        
        if (!(descriptor instanceof EjbSessionDescriptor) &&
                !(descriptor instanceof EjbEntityDescriptor)) {
            addNaDetails(result, compName);
            result.notApplicable(smh.getLocalString
                    ("com.sun.enterprise.tools.verifier.tests.ejb.homeintf.HomeMethodTest.notApplicable1",
                    "Test apply only to session or entity beans."));
            return result;                
        }
        
        if(descriptor.getRemoteClassName() == null || "".equals(descriptor.getRemoteClassName())){
            addNaDetails(result, compName);
            result.notApplicable(smh.getLocalString
                    ("com.sun.enterprise.tools.verifier.tests.ejb.intf.InterfaceTest.notApplicable",
                    "Not Applicable because, EJB [ {0} ] does not have {1} Interface.",
                    new Object[] {descriptor.getEjbClassName(), "Remote"}));
            
            return result;
        }
        
        
        boolean oneFailed = false;
        try {
            ClassLoader jcl = getVerifierContext().getClassLoader();
            Class c = Class.forName(descriptor.getRemoteClassName(), false, jcl);
            Class remote = c;
            boolean validRemoteInterface = false;
            boolean ok = false;
            // walk up the class tree
            do {
                Class[] interfaces = c.getInterfaces();
                if ( interfaces.length == 0 ) {
                    ok = true;
                } 
                for (Class intf : interfaces) {
                    logger.log(Level.FINE, getClass().getName() + ".debug1",
                            new Object[] {intf.getName()});
                    
                    //  The remote interface is allowed to have superinterfaces. Use
                    //  of interface inheritance is subject to the RMI-IIOP rules for
                    //  the definition of remote interfaces.
                    // requirement is met if one superinterface complies.
                    if (!ok) {
                        ok = RmiIIOPUtils.isValidRmiIIOPInterface(intf);
                    }
                    
                    // check the methods now.
                    if (RmiIIOPUtils.isValidRmiIIOPInterfaceMethods(intf)) {
                        // this interface is valid, continue
                        if (intf.getName().equals("jakarta.ejb.EJBObject")) {
                            validRemoteInterface = true;
                            break;
                        }
                    } else {
                        oneFailed = true;
                        addErrorDetails(result, compName);
                        result.addErrorDetails(smh.getLocalString
                                (getClass().getName() + ".failed",
                                "Error: [ {0} ] does not properly conform to " +
                                "rules of RMI-IIOP for superinterfaces.  All " +
                                "enterprise beans remote interfaces are allowed " +
                                "to have superinterfaces that conform to the " +
                                "rules of RMI-IIOP for superinterfaces .  [ {1} ]" +
                                " is not a valid remote interface.",
                                new Object[] {intf.getName(),descriptor.getRemoteClassName()}));
                    }
                    
                }
                
            } while ((((c=c.getSuperclass()) != null) && (!validRemoteInterface)));
            
            if (!ok) {  // check that one superinterface met rmiiiop requirement
                oneFailed = true;
                addErrorDetails(result, compName);
                result.addErrorDetails(smh.getLocalString
                        (getClass().getName() + ".failed",
                        "Error: [ {0} ] does not properly conform to rules of " +
                        "RMI-IIOP for superinterfaces.  All enterprise beans " +
                        "remote interfaces are allowed to have superinterfaces " +
                        "that conform to the rules of RMI-IIOP for superinterfaces. " +
                        " [ {1} ] is not a valid remote interface.",
                        new Object[] {remote.getName(),descriptor.getRemoteClassName()}));
            }
            
            if (validRemoteInterface){
                addGoodDetails(result, compName);
                result.passed(smh.getLocalString
                        (getClass().getName() + ".passed",
                        "[ {0} ] properly conforms to rules of RMI-IIOP for superinterfaces.",
                        new Object[] {descriptor.getRemoteClassName()}));
            }
        } catch (ClassNotFoundException e) {
            Verifier.debug(e);
            addGoodDetails(result, compName);
            result.failed(smh.getLocalString
                    (getClass().getName() + ".failedException",
                    "Error: Remote interface [ {0} ] does not exist or is not " +
                    "loadable within bean [ {1} ]",
                    new Object[] {descriptor.getRemoteClassName(),descriptor.getName()}));
            oneFailed = true;
        }
        if (oneFailed) {
            result.setStatus(Result.FAILED);
        } else {
            result.setStatus(Result.PASSED);
        }
        return result;
    }
}
