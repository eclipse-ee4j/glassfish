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

package com.sun.enterprise.tools.verifier.tests.connector.managed;

import java.io.File;
import java.lang.reflect.Method;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.tools.verifier.tests.*;
import com.sun.enterprise.tools.verifier.tests.connector.ConnectorTest;
import com.sun.enterprise.tools.verifier.tests.connector.ConnectorCheck;

/**
 * Test that the return type of
 * jakarta.resource.spi.ManagedConnection.getMetaData() implements 
 * the ManagedConnectionMetaData interface.
 *
 * @author Anisha Malhotra 
 * @version 
 */
public class ManagedConnectionGetMetaData
    extends ConnectorTest 
    implements ConnectorCheck 
{
  /** <p>
   * Test that the return type of
   * jakarta.resource.spi.ManagedConnection.getMetaData() implements 
   * the ManagedConnectionMetaData interface.
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
    // test NA for inboundRA
    if(!descriptor.getOutBoundDefined())
    {
      result.addNaDetails(smh.getLocalString
          ("tests.componentNameConstructor",
           "For [ {0} ]",
           new Object[] {compName.toString()}));
      result.notApplicable(smh.getLocalString
          ("com.sun.enterprise.tools.verifier.tests.connector.managed.notApplicableForInboundRA",
           "Resource Adapter does not provide outbound communication"));
      return result;
    }
	//File jarFile = Verifier.getJarFile(descriptor.getModuleDescriptor().getArchiveUri());
//        File f=Verifier.getArchiveFile(descriptor.getModuleDescriptor().getArchiveUri());
        Class c = findImplementorOf(descriptor, "jakarta.resource.spi.ManagedConnection");
    if(c == null)
    {
      result.addErrorDetails(smh.getLocalString
          ("tests.componentNameConstructor",
           "For [ {0} ]",
           new Object[] {compName.toString()}));
      result.failed(smh.getLocalString
          ("com.sun.enterprise.tools.verifier.tests.connector.ConnectorTest.findImplementor.failed", 
           "Error: There is no implementation of the [ {0} ] provided",
           new Object[] {"jakarta.resource.spi.ManagedConnection"}));        
      return result;
    }
    // get return type of getMetaData()
    Method m = null;
    do {
      try {
        m = c.getMethod("getMetaData", (Class[])null);
      } catch(NoSuchMethodException nsme) {
      } catch(SecurityException se) {
      }
      c = c.getSuperclass();
    } while (m != null && c != Object.class);            
    if(m == null)
    {
      result.addErrorDetails(smh.getLocalString
          ("tests.componentNameConstructor",
           "For [ {0} ]",
           new Object[] {compName.toString()}));
      result.failed(smh.getLocalString
          ("com.sun.enterprise.tools.verifier.tests.connector.managed.ManagedConnectionGetMetaData.failed", 
           "Error: There is no implementation of getMetaData() provided"));
      return result;
    }
    Class returnType = m.getReturnType();
    if(VerifierTest.isImplementorOf(returnType, 
          "jakarta.resource.spi.ManagedConnectionMetaData"))
    {
      result.addGoodDetails(smh.getLocalString
          ("tests.componentNameConstructor",
           "For [ {0} ]",
           new Object[] {compName.toString()}));
      result.passed(smh.getLocalString
          ("com.sun.enterprise.tools.verifier.tests.connector.managed.ManagedConnectionGetMetaData.passed", 
           "ManagedConnection.getMetaData() returns an instance of the" + 
           "jakarta.resource.spi.ManagedConnectionMetaData interface"));
    }
    else
    {
      result.addErrorDetails(smh.getLocalString
          ("tests.componentNameConstructor",
           "For [ {0} ]",
           new Object[] {compName.toString()}));
      result.failed(smh.getLocalString
          ("com.sun.enterprise.tools.verifier.tests.connector.managed.ManagedConnectionGetMetaData.failed1", 
           "Error: getMetaData() does not return an instance of the" + 
           "jakarta.resource.spi.ManagedConnectionMetaData interface"));
    }
    return result;
  }
}
