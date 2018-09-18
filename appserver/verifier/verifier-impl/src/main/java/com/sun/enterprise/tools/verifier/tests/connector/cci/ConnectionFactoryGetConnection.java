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
 * ConnectionFactoryGetConnection.java
 *
 * Created on October 3, 2000, 5:41 PM
 */

package com.sun.enterprise.tools.verifier.tests.connector.cci;

import java.lang.reflect.Method;
import com.sun.enterprise.tools.verifier.tests.connector.ConnectorCheck;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.tools.verifier.tests.*;

/**
 * Check that the getConnection method of the client API Connection factory
 * is implemented accordingly to the spec
 * @author  Jerome Dochez
 * @version 
 */
public class ConnectionFactoryGetConnection 
extends ConnectionFactoryTest 
implements ConnectorCheck 
{

  /**
   * <p>
   * all connector tests should implement this method. it run an individual
   * test against the resource adapter deployment descriptor. 
   * </p>
   *
   * @paramm descriptor deployment descriptor for the rar file
   * @return result object containing the result of the individual test
   * performed
   */    
  public Result check(ConnectorDescriptor descriptor) {

    Result result = getInitializedResult();
    ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
    if(isCCIImplemented(descriptor, result))
    {
      Class cf = testConnectionFactoryImpl(descriptor, result);
      if (cf == null) 
        return result;
      String className = cf.getName();

      do {
        Method[] allMethods = cf.getMethods();
        for (int i=0;i<allMethods.length;i++) {
          if (allMethods[i].getName().equals("getConnection")) {
            // found it, check the return type
            String connection = getConnectionInterface(descriptor, result);
            if (isSubclassOf(allMethods[i].getReturnType(), connection)) {
              result.addGoodDetails(smh.getLocalString
                  ("tests.componentNameConstructor",
                   "For [ {0} ]",
                   new Object[] {compName.toString()}));
              result.passed(smh.getLocalString(
                    getClass().getName() + ".passed",
                    "The getConnection method of the [ {0} ] returns the [ {1} ] interface",
                    new Object[] {cf.getName(), connection} ));        
            } else {
              result.addErrorDetails(smh.getLocalString
                  ("tests.componentNameConstructor",
                   "For [ {0} ]",
                   new Object[] {compName.toString()}));
              result.failed(smh.getLocalString(                        
                    getClass().getName() + ".failed",
                    "Error: The getConnection method of the [ {0} ] does not return the [ {1} ] interface",
                    new Object[] {cf.getName(), connection} ));        
            }
            return result;
          } 
        }
        cf = cf.getSuperclass();
      } while (cf!=null);
      result.addWarningDetails(smh.getLocalString
          ("tests.componentNameConstructor",
           "For [ {0} ]",
           new Object[] {compName.toString()}));
      result.warning(smh.getLocalString(
            getClass().getName() + ".warning",
            "Warning: The getConnection method is not defined by [ {0} ]",
            new Object[] {className} ));
    }
    else
    {
      result.addNaDetails(smh.getLocalString
          ("tests.componentNameConstructor",
           "For [ {0} ]",
           new Object[] {compName.toString()}));
      result.notApplicable(smh.getLocalString
          ("com.sun.enterprise.tools.verifier.tests.connector.cci.notApp",
           "The CCI interfaces do not seem to be implemented by this resource adapter"));
    }
    return result;
  }
}
