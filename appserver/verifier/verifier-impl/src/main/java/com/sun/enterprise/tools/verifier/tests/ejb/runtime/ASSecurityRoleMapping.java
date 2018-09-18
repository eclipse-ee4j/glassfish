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

package com.sun.enterprise.tools.verifier.tests.ejb.runtime;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

/** ias-ejb-jar
 *    security-role-mapping [0,n]
 *        role-name [String]
 *        principal-name [String] | group-name [String]
 *
 * The element defines the security role mappings for the bean.
 * The role-name should not be an empty string
 * The role-name should be desclared in the assembly-descriptor in the ejb-jar.xml
 * file.
 * The principal-name and group-name should not be empty strings.
 *
 * @author Irfan Ahmed
 */
public class ASSecurityRoleMapping extends EjbTest implements EjbCheck { 
//hs NO API for security-role-mapping element from sun-ejb-jar.xml
//hs DOL Issue - information missing

    public Result check(EjbDescriptor descriptor)
    {
        Result result = getInitializedResult();
/*
 *        
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        boolean oneFailed = false;
        SunEjbJar ejbJar = descriptor.getEjbBundleDescriptor().getIasEjbObject();
        
        if(descriptor.getEjbBundleDescriptor().getTestsDone().contains(getClass().getName()))
        {
            result.setStatus(Result.NOT_RUN);
            result.addGoodDetails(smh.getLocalString("iasEjbJar.allReadyRun",
                "NOT RUN [AS-EJB ias-ejb-jar] security-role-mapping is a JAR Level Test. This test has already been run once"));
            return result;
        }
        descriptor.getEjbBundleDescriptor().setTestsDone(getClass().getName());
        
        if(ejbJar!=null)
        {
            SecurityRoleMapping secRoleMapping[] = ejbJar.getSecurityRoleMapping();
            if(secRoleMapping.length>0)
            {
                for(int i=0;i<secRoleMapping.length;i++)
                {
                    String roleName = secRoleMapping[i].getRoleName();
                    if(roleName.length()==0)
                    {
                        oneFailed = true;
                        result.failed(smh.getLocalString(getClass().getName()+".failed",
                            "FAILED [AS-EJB security-role-mapping] : role-name cannot be an empty string",
                            new Object[]{new Integer(i)}));
                    }
                    else
                    {
                        boolean roleNameFound = false;
                        Set roles = descriptor.getEjbBundleDescriptor().getRoles();
                        Iterator it = roles.iterator();
                        while(it.hasNext())
                        {
                            Role role = (Role)it.next();
                            if(role.getName().equals(roleName))
                            {
                                roleNameFound = true;
                                break;
                            }
                        }
                        if(roleNameFound)
                        {
                            result.passed(smh.getLocalString(getClass().getName()+".passed",
                                "PASSED [AS-EJB security-role-mapping] : role-name {1} verified with ejb-jar.xml",
                                new Object[]{new Integer(i), roleName}));
                        }
                        else
                        {
                            oneFailed = true;
                            //<addition> srini@sun.com Bug: 4721914
                            //result.failed(smh.getLocalString(getClass().getName()+".failed",
                            result.failed(smh.getLocalString(getClass().getName()+".failed1",
                                "FAILED [AS-EJB security-role-mapping] : role-name {1} could not be located in ejb-jar.xml",
                                new Object[]{new Integer(i), roleName}));
                            //<addition>
                        }
                    }

                    String pName[] = secRoleMapping[i].getPrincipalName();
                    for(int j=0;j<pName.length;j++)
                    {
                        if(pName[j].length()==0)
                        {
                            oneFailed = true;
                            //<addition> srini@sun.com Bug: 4721914
                            //result.failed(smh.getLocalString(getClass().getName()+".failed",
                            result.failed(smh.getLocalString(getClass().getName()+".failed2",
                                "FAILED [AS-EJB security-role-mapping] : principal-name cannot be empty string",
                                new Object[]{new Integer(i)}));
                            //<addition>
                        }
                        else
                        {
                            //<addition> srini@sun.com Bug: 4721914
                            //result.passed(smh.getLocalString(getClass().getName()+".passed",
                            result.passed(smh.getLocalString(getClass().getName()+".passed1",
                                "PASSED [AS-EJB security-role-mapping] : principal-name is {1}",
                                new Object[]{new Integer(i),pName[j]}));
                            //<addition>
                        }
                    }

                    pName = secRoleMapping[i].getGroupName();
                    for(int j=0;j<pName.length;j++)
                    {
                        if(pName[j].length()==0)
                        {
                            oneFailed = true;
                            //<addition> srini@sun.com Bug: 4721914
                            //result.failed(smh.getLocalString(getClass().getName()+".failed",
                            result.failed(smh.getLocalString(getClass().getName()+".failed3",
                                "FAILED [AS-EJB security-role-mapping] : group-name cannot be empty string",
                                new Object[]{new Integer(i)}));
                            //<addition>
                        }
                        else
                        {
                            //<addition> srini@sun.com Bug: 4721914
                            //result.passed(smh.getLocalString(getClass().getName()+".passed",
                            result.passed(smh.getLocalString(getClass().getName()+".passed2",
                                "PASSED [AS-EJB security-role-mapping] : group-name is {1}",
                                new Object[]{new Integer(i),pName[j]}));
                            //<addition>
                        }
                    }
                }
            }
            else
            {
                result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable",
                    "NOT APPLICABLE [AS-EJB] : security-role-mapping element is not defined"));
            }
            if(oneFailed)
                result.setStatus(Result.FAILED);
        }
        else
        {
            result.addErrorDetails(smh.getLocalString
                                   ("tests.componentNameConstructor",
                                    "For [ {0} ]",
                                    new Object[] {compName.toString()}));
            result.addErrorDetails(smh.getLocalString
                 (getClass().getName() + ".notRun",
                  "NOT RUN [AS-EJB] : Could not create an SunEjbJar object"));
        }
 */
        return result;
    }
}
        
