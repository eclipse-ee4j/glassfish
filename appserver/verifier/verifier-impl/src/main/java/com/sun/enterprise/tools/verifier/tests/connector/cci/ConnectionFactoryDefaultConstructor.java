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
 * ManagedConnectionGetMetaData.java
 *
 * Created on August 26, 2002
 */

package com.sun.enterprise.tools.verifier.tests.connector.cci;

import java.io.File;
import java.lang.reflect.Method;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.tools.verifier.tests.*;
import com.sun.enterprise.tools.verifier.tests.connector.ConnectorCheck;

/**
 * Test that the implementation class for  
 * javax.resource.cci.ConnectionFactory provides a default constructor 
 *
 * @author Anisha Malhotra 
 * @version 
 */
public class ConnectionFactoryDefaultConstructor
    extends ConnectionFactoryTest 
    implements ConnectorCheck 
{
  /** <p>
   * Test that the implementation class for  
   * javax.resource.cci.ConnectionFactory provides a default constructor 
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
    Class connFactoryImpl = null;
    if(isCCIImplemented(descriptor, result))
    {
      connFactoryImpl = testConnectionFactoryImpl(descriptor, result);
      if (connFactoryImpl == null) 
        return result;
    }
    else
    {
      // test is NA
      result.addNaDetails(smh.getLocalString
          ("tests.componentNameConstructor",
           "For [ {0} ]",
           new Object[] {compName.toString()}));
      result.notApplicable(smh.getLocalString
          ("com.sun.enterprise.tools.verifier.tests.connector.cci.notApp",
           "The CCI interfaces do not seem to be implemented by this resource adapter"));
      return result;
    }
    // check if connectionfactory-impl-class has a default constructor
    try
    {
      connFactoryImpl.getConstructor(new Class[0]);
      result.addGoodDetails(smh.getLocalString
          ("tests.componentNameConstructor",
           "For [ {0} ]",
           new Object[] {compName.toString()}));
      result.passed(smh.getLocalString
          ("com.sun.enterprise.tools.verifier.tests.connector.cci.ConnectionFactoryDefaultConstructor.defConstr", 
           "The connectionfactory-impl-class: [ {0} ] provides a default constructor.", new Object[] {connFactoryImpl.getName()} ));    
    }
    catch(NoSuchMethodException nsme)
    {
      result.addErrorDetails(smh.getLocalString
          ("tests.componentNameConstructor",
           "For [ {0} ]",
           new Object[] {compName.toString()}));
      result.failed(smh.getLocalString
          ("com.sun.enterprise.tools.verifier.tests.connector.cci.ConnectionFactoryDefaultConstructor.noDefConstr", 
           "Error: The connectionfactory-impl-class: [ {0} ] must provide a default constructor.", new Object[] {connFactoryImpl.getName()} ));    
    }
    catch(SecurityException se)
    {
    }
    return result;
  }
}
