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
 * CheckAdminObjectImpl.java
 *
 * Created on August 29, 2002
 */

package com.sun.enterprise.tools.verifier.tests.connector.admin;

import com.sun.enterprise.tools.verifier.tests.connector.ConnectorTest;
import com.sun.enterprise.tools.verifier.tests.connector.ConnectorCheck;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.AdminObject;
import com.sun.enterprise.tools.verifier.tests.*;
import java.util.Iterator;
import java.util.Set;

/**
 * Test for each adminobject, that "adminobject-class"
 * implements "adminobject-interface".
 *
 * @author Anisha Malhotra 
 * @version 
 */
public class CheckAdminObjectImpl
    extends ConnectorTest 
    implements ConnectorCheck 
{

  /** <p>
   * Test for each adminobject, that "adminobject-class"
   * implements "adminobject-interface".
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
    if(!descriptor.hasAdminObjects())
    {
      result.addNaDetails(smh.getLocalString
          ("tests.componentNameConstructor",
           "For [ {0} ]",
           new Object[] {compName.toString()}));
      result.notApplicable(smh.getLocalString
          ("com.sun.enterprise.tools.verifier.tests.connector.admin.notApp",
           "Resource Adapter does not define any administered objects"));
      return result;
    }
    Set adminObjects = descriptor.getAdminObjects();
    boolean oneFailed = false;
    Iterator iter = adminObjects.iterator();
    while(iter.hasNext()) 
    {
      AdminObject adminObj = (AdminObject) iter.next();
      String intf = adminObj.getAdminObjectInterface();
      String impl = adminObj.getAdminObjectClass();
      Class implClass = null;
      try
      {
        implClass = Class.forName(impl, false, getVerifierContext().getClassLoader());
      }
      catch(ClassNotFoundException e)
      {
        result.addErrorDetails(smh.getLocalString
            ("tests.componentNameConstructor",
             "For [ {0} ]",
             new Object[] {compName.toString()}));
        result.failed(smh.getLocalString
            ("com.sun.enterprise.tools.verifier.tests.connector.admin.nonexist",
             "Error: The class [ {0} ] as defined under adminobject-class in the deployment descriptor does not exist",
             new Object[] {impl}));
        return result;
      }
      if(!isImplementorOf(implClass, intf))
      {
        oneFailed = true;
        result.addErrorDetails(smh.getLocalString
            ("tests.componentNameConstructor",
             "For [ {0} ]",
             new Object[] {compName.toString()}));
        result.failed(smh.getLocalString(getClass().getName() + ".failed",
              "Error: adminobject-class [ {0} ] does not implement adminobject-interface [ {1} ].",
              new Object[] {impl, intf}));
        return result;                
      }
    }
    if(!oneFailed)
    {
      result.addGoodDetails(smh.getLocalString
          ("tests.componentNameConstructor",
           "For [ {0} ]",
           new Object[] {compName.toString()}));	
      result.passed(smh.getLocalString(getClass().getName() + ".passed",
            "Success: all adminobject-class implement their corresponding adminobject-interface"));                     
    }
    return result;
  }
}
