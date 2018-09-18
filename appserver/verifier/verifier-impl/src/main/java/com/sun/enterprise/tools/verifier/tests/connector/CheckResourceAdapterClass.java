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

/*
 * CheckResourceAdapterClass.java
 *
 * Created on August 29, 2002
 */

package com.sun.enterprise.tools.verifier.tests.connector;

import com.sun.enterprise.tools.verifier.tests.connector.ConnectorTest;
import com.sun.enterprise.tools.verifier.tests.connector.ConnectorCheck;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.tools.verifier.tests.*;

/**
 * Test that "resourceadapter-class" implements 
 * "javax.resource.spi.ResourceAdapter".
 *
 * @author Anisha Malhotra 
 * @version 
 */
public class CheckResourceAdapterClass
extends ConnectorTest 
implements ConnectorCheck 
{

  /** <p>
   * Test that "resourceadapter-class" implements 
   * "javax.resource.spi.ResourceAdapter".
   * </p>
   *
   * @param descriptor deployment descriptor for the rar file
   * @return result object containing the result of the individual test
   * performed
   */
  public Result check(ConnectorDescriptor descriptor) {

    Result result = getInitializedResult();
    ComponentNameConstructor compName = 
      getVerifierContext().getComponentNameConstructor();
    String resourceAdapterClass = descriptor.getResourceAdapterClass();
    if(resourceAdapterClass.equals(""))
    {
      if(descriptor.getInBoundDefined())
      {
        // resourceadapter-class cannot be null
        result.addErrorDetails(smh.getLocalString
            ("tests.componentNameConstructor",
             "For [ {0} ]",
             new Object[] {compName.toString()}));
        result.failed(smh.getLocalString
            (getClass().getName() + ".failed1",
             "resourceadapter-class cannot be empty if the resource" + 
             " adapter provides inbound communication"));
      }
      else
      {
          result.addNaDetails(smh.getLocalString
              ("tests.componentNameConstructor",
               "For [ {0} ]",
               new Object[] {compName.toString()}));
          result.notApplicable(smh.getLocalString
              ("com.sun.enterprise.tools.verifier.tests.connector.resourceadapter.notApp",
               "resourceadapter-class is not specified."));
      }
      return result;
    }
    VerifierTestContext context = getVerifierContext();
    ClassLoader jcl = context.getRarClassLoader();
    Class implClass = null;
    try
    {
      implClass = Class.forName(resourceAdapterClass, false, getVerifierContext().getClassLoader());    
    }
    catch(ClassNotFoundException e)
    {
      result.addErrorDetails(smh.getLocalString
          ("tests.componentNameConstructor",
           "For [ {0} ]",
           new Object[] {compName.toString()}));
      result.failed(smh.getLocalString
          ("com.sun.enterprise.tools.verifier.tests.connector." + 
          "CheckResourceAdapter.nonexist",
           "Error: The class [ {0} ] as defined under resourceadapter-class " +            "in the deployment descriptor does not exist",
           new Object[] {resourceAdapterClass}));
      return result;
    }
    if(!isImplementorOf(implClass, "javax.resource.spi.ResourceAdapter"))
    {
      result.addErrorDetails(smh.getLocalString
          ("tests.componentNameConstructor",
           "For [ {0} ]",
           new Object[] {compName.toString()}));
      result.failed(smh.getLocalString(getClass().getName() + ".failed",
            "Error: resourceadapter-class [ {0} ] does not implement javax.resource.spi.ResourceAdapter",
            new Object[] {resourceAdapterClass}));
    }
    else
    {
      result.addGoodDetails(smh.getLocalString
          ("tests.componentNameConstructor",
           "For [ {0} ]",
           new Object[] {compName.toString()}));	
      result.passed(smh.getLocalString(getClass().getName() + ".passed",
            "Success: resourceadapter-class implements javax.resource.spi.ResourceAdapter"));                     
    }
    return result;                
  }
}
