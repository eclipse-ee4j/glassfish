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
 * CheckConnectionFactoryImplReferenceable.java
 *
 * Created on August 29, 2002
 */

package com.sun.enterprise.tools.verifier.tests.connector.managed;

import com.sun.enterprise.tools.verifier.tests.connector.ConnectorTest;
import com.sun.enterprise.tools.verifier.tests.connector.ConnectorCheck;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.ConnectionDefDescriptor;
import com.sun.enterprise.deployment.OutboundResourceAdapter;
import com.sun.enterprise.tools.verifier.tests.*;
import java.util.Iterator;
import java.util.Set;

/**
 * Test for each connection-definition, that "connectionfactory-impl-class"
 * implements javax.resource.Referenceable
 *
 * @author Anisha Malhotra 
 * @version 
 */
public class CheckConnectionFactoryImplReferenceable
    extends ConnectorTest 
    implements ConnectorCheck 
{

  /** <p>
   * Test for each connection-definition, that "connectionfactory-impl-class"
   * implements javax.resource.Referenceable
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
    OutboundResourceAdapter outboundRA =
      descriptor.getOutboundResourceAdapter();
    if(outboundRA == null)
    {
      return null;
    }
    boolean oneFailed = false;
    Set connDefs = outboundRA.getConnectionDefs();
    Iterator iter = connDefs.iterator();
    while(iter.hasNext()) 
    {
      ConnectionDefDescriptor connDefDesc = (ConnectionDefDescriptor)
        iter.next();
      String connectionImpl = connDefDesc.getConnectionFactoryImpl();
      Class implClass = null;
      try
      {
        implClass = Class.forName(connectionImpl, false, getVerifierContext().getClassLoader());
      }
      catch(ClassNotFoundException e)
      {
        result.addErrorDetails(smh.getLocalString
            ("tests.componentNameConstructor",
             "For [ {0} ]",
             new Object[] {compName.toString()}));
        result.failed(smh.getLocalString
            (getClass().getName() + ".nonexist",
             "Error: The class [ {0} ] as defined under connectionfactory-impl-class in the deployment descriptor does not exist",
             new Object[] {connectionImpl}));
        return result;
      }
      if(!isImplementorOf(implClass, "javax.resource.Referenceable"))
      {
        oneFailed = true;
        result.addErrorDetails(smh.getLocalString
            ("tests.componentNameConstructor",
             "For [ {0} ]",
             new Object[] {compName.toString()}));
        result.failed(smh.getLocalString(getClass().getName() + ".failed",
              "Error: connectionfactory-impl-class [ {0} ] does not implement javax.resource.Referenceable",
              new Object[] {implClass.getName()}));
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
            "Success: all connectionfactory-impl-class implement javax.resource.Referenceable"));                     
    }
    return result;
  }
}
