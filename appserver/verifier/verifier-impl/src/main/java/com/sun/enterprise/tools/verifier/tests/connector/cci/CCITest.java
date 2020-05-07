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
 * CCITest.java
 *
 * Created on August 28, 2002
 */

package com.sun.enterprise.tools.verifier.tests.connector.cci;

import com.sun.enterprise.tools.verifier.tests.connector.ConnectorTest;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.ConnectionDefDescriptor;
import com.sun.enterprise.deployment.OutboundResourceAdapter;
import com.sun.enterprise.tools.verifier.tests.*;
import java.lang.ClassLoader;
import java.util.Iterator;
import java.util.Set;
/**
 * Contains helper methods for all tests pertinent to CCI 
 *
 * @author Anisha Malhotra 
 * @version 
 */
public abstract class CCITest extends ConnectorTest {

  /**
   * <p>
   * Checks whether the resource adapater is implementing the CCI interfaces
   * </p>
   * @param descriptor the deployment descriptor
   * @param result to put the result
   * @return true if the CCI is implemented
   */
  protected boolean isCCIImplemented(ConnectorDescriptor descriptor, 
      Result result) {
    ComponentNameConstructor compName = 
      getVerifierContext().getComponentNameConstructor();
    OutboundResourceAdapter outboundRA =
      descriptor.getOutboundResourceAdapter();
    if(outboundRA == null)
    {
      return false;
    }
    Set connDefs = outboundRA.getConnectionDefs();
    Iterator iter = connDefs.iterator();
    while(iter.hasNext()) 
    {
      ConnectionDefDescriptor connDefDesc = (ConnectionDefDescriptor)
        iter.next();
      // check if intf implements jakarta.resource.cci.ConnectionFactory
      String intf = connDefDesc.getConnectionFactoryIntf();
      Class implClass = null;
      try
      {
        implClass = Class.forName(intf, false, getVerifierContext().getClassLoader());
      }
      catch(ClassNotFoundException e)
      {
      result.addErrorDetails(smh.getLocalString
          ("tests.componentNameConstructor",
           "For [ {0} ]",
           new Object[] {compName.toString()}));
        result.failed(smh.getLocalString
            ("com.sun.enterprise.tools.verifier.tests.connector.ConnectorTest.isClassLoadable.failed", 
             "The class [ {0} ] is not contained in the archive file",
             new Object[] {intf}));                
        continue;
      }
      if(isImplementorOf(implClass, "jakarta.resource.cci.ConnectionFactory"))
      {
        return true;
      }
    }
    return false;
  }


  /**
   * <p>
   * Returns the connection-interface that implements
   * "jakarta.resource.cci.Connection"
   * </p>
   * @param descriptor the deployment descriptor
   * @param result to put the result
   * @return interface name 
   */
  protected String getConnectionInterface(ConnectorDescriptor descriptor,
      Result result)
  {
    ComponentNameConstructor compName = 
      getVerifierContext().getComponentNameConstructor();
    OutboundResourceAdapter outboundRA =
      descriptor.getOutboundResourceAdapter();
    if(outboundRA == null)
    {
      return null;
    }
    Set connDefs = outboundRA.getConnectionDefs();
    Iterator iter = connDefs.iterator();
    while(iter.hasNext()) 
    {
      ConnectionDefDescriptor connDefDesc = (ConnectionDefDescriptor)
        iter.next();
      String intf = connDefDesc.getConnectionIntf();
      VerifierTestContext context = getVerifierContext();
      ClassLoader jcl = context.getRarClassLoader();
      Class intfClass = null;
      try
      {
        intfClass = Class.forName(intf, false, getVerifierContext().getClassLoader());    
      }
      catch(ClassNotFoundException e)
      {
        result.addErrorDetails(smh.getLocalString
            ("tests.componentNameConstructor",
             "For [ {0} ]",
             new Object[] {compName.toString()}));
        result.failed(smh.getLocalString
            ("com.sun.enterprise.tools.verifier.tests.connector.ConnectorTest.isClassLoadable.failed", 
             "The class [ {0} ] is not contained in the archive file",
             new Object[] {intf}));                
        continue;
      }
      if(isImplementorOf(intfClass, "jakarta.resource.cci.Connection"))
      {
        return intf;
      }
    }
    return null;
  }

}
