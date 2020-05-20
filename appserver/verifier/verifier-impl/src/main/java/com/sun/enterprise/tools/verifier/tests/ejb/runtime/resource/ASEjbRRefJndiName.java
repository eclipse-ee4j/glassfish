/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

/** ejb [0,n]
 *   resource-ref [0,n]
 *       res-ref-name [String]
 *       jndi-name [String]
 *       default-resource-principal ?
 *           name [String]
 *           password [String]
 *
 * The jndi-name specifies the JNDI name to which this resource is binded
 * The jndi-name should not be null.
 * The jndi-name should map to the correct subcontext and hence start with the
 * valid subcontext
 *    URL url/
 *    Mail mail/
 *    JDBC jdbc/
 *    JMS jms/
 *
 * @author Irfan Ahmed
 */

public class ASEjbRRefJndiName extends ASEjbResRef { 

    public Result check(EjbDescriptor descriptor)
    {
	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        boolean oneFailed = false;
        //boolean oneWarning = false;
   
        try{
        Set resRef = descriptor.getResourceReferenceDescriptors();
        if(!(resRef.isEmpty()))
        {
            Iterator it = resRef.iterator();
            while (it.hasNext())
            {
                ResourceReferenceDescriptor resDesc = ((ResourceReferenceDescriptor)it.next());
                String refName = resDesc.getName();
                String refJndiName = resDesc.getJndiName();
                String type = resDesc.getType();
                
                if(refJndiName == null || refJndiName.trim().equals(""))
                {
                    oneFailed = true;
                    result.failed(smh.getLocalString(getClass().getName()+".failed",
                            "FAILED [AS-EJB resource-ref]: jndi-name is not a non empty string"));
                }
                    /* else  //Fix for bug id 5018617
                    {
                        if(type.indexOf("jakarta.jms")>-1) //jms resource
                        {
                            if(refJndiName.startsWith("jms/")) {
                                addGoodDetails(result, compName);
                                result.passed(smh.getLocalString(getClass().getName()+".passed1",
                                    "PASSED [AS-EJB resource-ref] : jndi-name {0} is valid", new Object[]{refJndiName}));
                            }
                            else
                            {
                                oneWarning = true;
                                addWarningDetails(result, compName);
                                result.warning(smh.getLocalString(getClass().getName()+".warning1",
                                    "WARNING [AS-EJB resource-ref] : jndi-name is \"{0}\" for resource type \"{1}\"." + 
                                    "The preferred jndi-name for JMS resources should start with jms/",
                                    new Object[]{refJndiName,type}));
                            }
                        }
                        else if(type.indexOf("javax.sql")>-1) //jdbc resource
                        {
                            if(refJndiName.startsWith("jdbc/")) {
                                addGoodDetails(result, compName);
                                result.passed(smh.getLocalString(getClass().getName()+".passed1",
                                    "PASSED [AS-EJB resource-ref] : jndi-name {0} is valid", new Object[]{refJndiName}));
                            }
                            else
                            {
                                oneWarning = true;
                                addWarningDetails(result, compName);
                                result.warning(smh.getLocalString(getClass().getName()+".warning2",
                                    "WARNING [AS-EJB resource-ref] : jndi-name is \"{0}\" for resource type \"{1}\"." + 
                                    "The preferred jndi-name for JDBC resources should start with jdbc/",
                                    new Object[]{refJndiName,type}));
                            }
                        }
                        else if(type.indexOf("java.net")>-1) //url resource
                        {
                            if(refJndiName.startsWith("http://"))//FIX should it start with http:// or url/http://
                            {
                                addGoodDetails(result, compName);
                                result.passed(smh.getLocalString(getClass().getName()+".passed1",
                                    "PASSED [AS-EJB resource-ref] : jndi-name {0} is valid", new Object[]{refJndiName}));
                            }
                            else
                            {
                                oneWarning = true;
                                addWarningDetails(result, compName);
                                result.warning(smh.getLocalString(getClass().getName()+".warning3",
                                    "WARNING [AS-EJB resource-ref] : jndi-name is \"{0}\" for resource type \"{1}\". " + 
                                    "The preferred jndi-name for URL resources should start with a url",
                                    new Object[]{refJndiName,type}));
                            }
                        }
                        else if(type.indexOf("jakarta.mail")>-1) //jms resource
                        {
                            if(refJndiName.startsWith("mail/")) {
                                addGoodDetails(result, compName);
                                result.passed(smh.getLocalString(getClass().getName()+".passed1",
                                    "PASSED [AS-EJB resource-ref] : jndi-name {0} is valid", new Object[]{refJndiName}));
                            }
                            else
                            {
                                oneWarning = true;
                                addWarningDetails(result, compName);
                                result.warning(smh.getLocalString(getClass().getName()+".warning4",
                                    "WARNING [AS-EJB resource-ref] : jndi-name is \"{0}\" for resource type \"{1}\"." + 
                                    "The preferred jndi-name for MAIL resources should start with mail/",
                                    new Object[]{refJndiName,type}));
                            }
                        }
                        else
                        {
                            addGoodDetails(result, compName);
                            result.passed(smh.getLocalString(getClass().getName()+".passed1","PASSED [AS-EJB resource-ref]: jndi-name {0} is valid",new Object[]{refJndiName}));
                        }
                    }*/
            }
        }
        else
        {
            addNaDetails(result, compName);
            result.notApplicable(smh.getLocalString
                (getClass().getName() + ".notApplicable",
                "NOT APPLICABLE [AS-EJB] : {0} Does not define any resource-ref Elements",
                new Object[] {descriptor.getName()}));
            return result;
        }
        }catch(Exception ex)
        {
            oneFailed = true;
            addErrorDetails(result, compName);
                result.addErrorDetails(smh.getLocalString
                 (getClass().getName() + ".notRun",
                  "NOT RUN [AS-EJB] : Could not create the descriptor object"));
            return result;
        }
        /*if(oneWarning)
            result.setStatus(Result.WARNING);*/
		if(oneFailed)
            result.setStatus(Result.FAILED);
        else {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString(getClass().getName() + ".passed",
                    "PASSED [AS-EJB resource-ref]: jndi name is specified correctly for the resource-references with in the application",
                    new Object[]{}));
        }
        return result;
    }
}
