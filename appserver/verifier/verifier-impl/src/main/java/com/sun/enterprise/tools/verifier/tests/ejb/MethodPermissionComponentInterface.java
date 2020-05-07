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

import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.tools.verifier.Result;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbSessionDescriptor;

import java.util.Iterator;
import java.util.Set;

/** 
 * Session Bean transaction demarcation type for all methods of remote 
 * interface test.  
 * The transaction attributes must be specified for the methods defined
 * in the bean's remote interface and all the direct and indirect 
 * superinterfaces of the remote interface, excluding the methods of
 * the jakarta.ejb.EJBObject interface.
 */
public class MethodPermissionComponentInterface extends EjbTest implements EjbCheck { 
    Result result  = null;
    
    /** 
     * All methods should have a 
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {
        
        result = getInitializedResult();
//        boolean oneFailed = false;
        
        try  {
            if (descriptor instanceof EjbSessionDescriptor || descriptor instanceof EjbEntityDescriptor) {
                
                Set methods = descriptor.getMethodDescriptors();
//		 Set methodPermissions = new HashSet();
                boolean noPermissions = false;
                
                for (Iterator i = methods.iterator(); i.hasNext();) {
                    MethodDescriptor md = (MethodDescriptor) i.next();
                    Set permissions = descriptor.getMethodPermissionsFor(md);
                    if (permissions.isEmpty() || (permissions == null)) {
                        result.addWarningDetails(smh.getLocalString
                                (getClass().getName() + ".failed",
                                        "Warning: Method [ {0} ] of EJB [ {1} ] does not have assigned security-permissions",
                                        new Object[] {md.getName(), descriptor.getName()}));
                        result.setStatus(result.WARNING);
                        noPermissions = true;
                    } 
                }
                
                if (!noPermissions) {
                    result.passed(smh.getLocalString
                            (getClass().getName() + ".passed",
                                    "Valid: All [ {0} ]EJB  interfaces methods have security-permissions assigned.",
                                    new Object[] {descriptor.getName()}));
                }
                
            } else {
                result.notApplicable(smh.getLocalString(
                        getClass().getName() + ".notApplicable", 
                        "The bean [ {0} ] is neither a Session nor Entity Bean",
                        new Object[] {descriptor.getName()}));
                return result;
            }
        } catch (Exception e) {
            result.failed(smh.getLocalString(
                    getClass().getName() + ".exception", 
                    "The test generated the following exception [ {0} ]",
                    new Object[] {e.getLocalizedMessage()}));
        }
        return result;
    }
    
}
