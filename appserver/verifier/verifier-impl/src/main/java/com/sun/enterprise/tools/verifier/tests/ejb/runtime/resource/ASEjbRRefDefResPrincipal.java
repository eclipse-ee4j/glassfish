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

package com.sun.enterprise.tools.verifier.tests.ejb.runtime.resource;

import java.util.Iterator;
import java.util.Set;

import com.sun.enterprise.deployment.ResourcePrincipal;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

/** ejb [0,n]
 *    resource-ref [0,n]
 *        res-ref-name [String]
 *        jndi-name [String]
 *        default-resource-principal ?
 *            name [String]
 *            password [String]
 *
 * The default-resource-principal specifies the principal for the
 * resource
 * The name and password should not be null.
 * The principal should be declared if the authorization type for the resource
 * in ejb-jar.xml is "Application"
 * @author Irfan Ahmed
 */

public class ASEjbRRefDefResPrincipal extends ASEjbResRef { 

    public Result check(EjbDescriptor descriptor)
    {
	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        boolean oneFailed = false;
        try{
            Set resRef = descriptor.getResourceReferenceDescriptors();
            if(!(resRef.isEmpty())){
                Iterator it = resRef.iterator();
                while (it.hasNext()){
                    ResourceReferenceDescriptor resDesc = ((ResourceReferenceDescriptor)it.next());
                    String refName = resDesc.getName();
                    String refJndiName = resDesc.getJndiName();
                    ResourcePrincipal resPrinci = resDesc.getResourcePrincipal();
                   if(resPrinci == null)
                    {
                        try
                        {
                           resDesc = descriptor.getResourceReferenceByName(refName);
                            String resAuth = resDesc.getAuthorization();
                            if(resAuth.equals(ResourceReferenceDescriptor.APPLICATION_AUTHORIZATION))
                            {
                               addErrorDetails(result, compName);
                               result.failed(smh.getLocalString(getClass().getName()+".failed",
                                    "FAILED [AS-EJB resource-ref] : res-auth for res-ref-name {0} is defined as Application." + 
                                    "Therefore the default-resource-principal should be supplied with valid properties",
                                    new Object[] {refName}));
                            }
                            else
                            {
                               addNaDetails(result, compName);
                               result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable",
                                    "NOT APPLICABLE [AS-EJB resource-ref] : default-resource-principal Element not defined"));
                            }
                        }
                        catch(IllegalArgumentException iaex)
                        {
                           addErrorDetails(result, compName);
                           result.failed(smh.getLocalString(getClass().getName()+".failed2",
                                "FAILED [AS-EJB resource-ref] : res-ref-name {0} is not defined in the ejb-jar.xml",
                                new Object[]{refName}));
                        }
                    }else
                    {
                        String name = resPrinci.getName();
                        if(name == null || name.length()==0)
                        {
                            oneFailed = true;
                            addErrorDetails(result, compName);
                            result.failed(smh.getLocalString(getClass().getName()+".failed3",
                                "FAILED [AS-EJB default-resource-principal] :  name cannot be an empty string"));
                        }
                        else
                        {
                           addGoodDetails(result, compName);
                           result.passed(smh.getLocalString(getClass().getName()+".passed",
                                "PASSED [AS-EJB default-resource-principal] : name is {0}",new Object[]{name}));
                        }
                        
                        String password = resPrinci.getPassword();
                        if(password == null || password.length()==0)
                        {
                           addWarningDetails(result, compName);
                           result.warning(smh.getLocalString(getClass().getName()+".warning1",
                                "WARNING [AS-EJB default-resource-principal] : password is an empty string"));
                        }
                        else
                        {
                            addGoodDetails(result, compName);
                            result.passed(smh.getLocalString(getClass().getName()+".passed1",
                                "PASSED [AS-EJB default-resource-principal] : password is  {0}",new Object[]{password}));
                        }
                        
                        if(oneFailed)
                            result.setStatus(Result.FAILED);
                    }
                
                }
            }
            else
            {
                addNaDetails(result, compName);
                result.notApplicable(smh.getLocalString
                         (getClass().getName() + ".notApplicable",
                          "{0} Does not define any resource-ref Elements"));
            }
        }catch(Exception ex)
        {
            addErrorDetails(result, compName);
            result.addErrorDetails(smh.getLocalString
                 (getClass().getName() + ".notRun",
                  "NOT RUN [AS-EJB] : Could not create the descriptor object"));        
        }
        return result;
    }
}
