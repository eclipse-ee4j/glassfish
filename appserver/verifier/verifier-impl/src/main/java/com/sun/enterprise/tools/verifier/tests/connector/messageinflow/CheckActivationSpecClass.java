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
 * CheckActivationSpecClass.java
 *
 * Created on August 29, 2002
 */

package com.sun.enterprise.tools.verifier.tests.connector.messageinflow;

import com.sun.enterprise.tools.verifier.tests.connector.ConnectorTest;
import com.sun.enterprise.tools.verifier.tests.connector.ConnectorCheck;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.InboundResourceAdapter;
import com.sun.enterprise.deployment.MessageListener;
import com.sun.enterprise.tools.verifier.tests.*;
import java.util.Iterator;
import java.util.Set;

/**
 * Test for each message-listener , that "activationspec-class"
 * implements "javax.resource.spi.ActivationSpec".
 *
 * @author Anisha Malhotra 
 * @version 
 */
public class CheckActivationSpecClass
extends ConnectorTest 
implements ConnectorCheck 
{

  /** <p>
   * Test for each message-listener , that "activationspec-class"
   * implements "javax.resource.spi.ActivationSpec".
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
    if(!descriptor.getInBoundDefined())
    {
      result.addNaDetails(smh.getLocalString
          ("tests.componentNameConstructor",
           "For [ {0} ]",
           new Object[] {compName.toString()}));
      result.notApplicable(smh.getLocalString
          ("com.sun.enterprise.tools.verifier.tests.connector.messageinflow.notApp",
           "Resource Adapter does not provide inbound communication"));
      return result;
    }
    InboundResourceAdapter ra = descriptor.getInboundResourceAdapter();
    Set msgListeners = ra.getMessageListeners();
    boolean oneFailed = false;
    Iterator iter = msgListeners.iterator();
    while(iter.hasNext()) 
    {
      MessageListener msgListener = (MessageListener) iter.next();
      String impl = msgListener.getActivationSpecClass();
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
            ("com.sun.enterprise.tools.verifier.tests.connector.messageinflow.nonexist",
             "Error: The class [ {0} ] as defined under activationspec-class in the deployment descriptor does not exist",
             new Object[] {impl}));
        return result;
      }
      if(!isImplementorOf(implClass, "javax.resource.spi.ActivationSpec"))
      {
        oneFailed = true;
        result.addErrorDetails(smh.getLocalString
            ("tests.componentNameConstructor",
             "For [ {0} ]",
             new Object[] {compName.toString()}));
        result.failed(smh.getLocalString(getClass().getName() + ".failed",
              "Error: activationspec-class [ {0} ] does not implement javax.resource.spi.ActivationSpec.",
              new Object[] {impl}));
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
            "Success: all activationspec-class implement javax.resource.spi.ActivationSpec"));
    }
    return result;
  }
}
