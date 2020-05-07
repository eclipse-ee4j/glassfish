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
 * ManagedConnectionFactoryTest.java
 *
 * Created on September 27, 2000, 11:29 AM
 */

package com.sun.enterprise.tools.verifier.tests.connector.managed;

import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.ConnectionDefDescriptor;
import com.sun.enterprise.deployment.OutboundResourceAdapter;
import com.sun.enterprise.tools.verifier.tests.*;
import com.sun.enterprise.tools.verifier.tests.connector.ConnectorTest;
import java.util.Set;
import java.util.Iterator;

/**
 * Superclass for all ManagedConnectionFactory related tests
 *
 * @author  Jerome Dochez
 * @version 
 */
public abstract class ManagedConnectionFactoryTest extends ConnectorTest {

    private String managedConnectionFactoryImpl;

    /**
     * <p>
     * Get the <code>Class</code> object of the class declared to be implementing
     * the jakarta.resource.spi.ManagedConnectionFactory interface in the 
     * archive deployment descriptor
     * </p> 
     *
     * @param descriptor the rar file deployment descriptor
     *
     * @throws ClassNotFoundException if the class identified by className 
     * cannot be loaded    
     */
    protected Class getManagedConnectionFactoryImpl(ConnectorDescriptor descriptor) 
        throws ClassNotFoundException 
    {
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
        managedConnectionFactoryImpl = 
          connDefDesc.getManagedConnectionFactoryImpl();
        Class implClass = Class.forName(managedConnectionFactoryImpl, false, getVerifierContext().getClassLoader());
        if(isImplementorOf(implClass, "jakarta.resource.spi.ManagedConnectionFactory"))
        {
          return implClass;
        }
      }
      return null;

      /*  String className = descriptor.getManagedConnectionFactoryImpl();
          if (className == null) 
          return null;

          VerifierTestContext context = getVerifierContext();
          ClassLoader jcl = context.getRarClassLoader();
          return jcl.loadClass(className);   */ 
    }


    /**
     * <p>
     * Test whether the class declared in the deployemnt descriptor under the 
     * managedconnecttionfactory-class tag is available
     * </p>
     * 
     * @param descriptor the deployment descriptor
     * @param result instance to use to put the result of the test
     * @return true if the test succeeds
     */
    protected Class testManagedConnectionFactoryImpl(ConnectorDescriptor descriptor, Result result) 
    {
      Class mcf = null;
      ComponentNameConstructor compName = null;
      try {
        compName = getVerifierContext().getComponentNameConstructor();
        mcf = getManagedConnectionFactoryImpl(descriptor);
        if (mcf == null) {
          result.addErrorDetails(smh.getLocalString
              ("tests.componentNameConstructor",
               "For [ {0} ]",
               new Object[] {compName.toString()}));
          result.failed(smh.getLocalString
              ("com.sun.enterprise.tools.verifier.tests.connector.managed.ManagedConnectionFactoryTest.nonimpl", 
               "Error: The resource adapter must implement the jakarta.resource.spi.ManagedConnectionFactory interface and declare it in the managedconnecttionfactory-class deployment descriptor."));                
        }
      } catch(ClassNotFoundException cnfe) {
        cnfe.printStackTrace();
        result.addErrorDetails(smh.getLocalString
            ("tests.componentNameConstructor",
             "For [ {0} ]",
             new Object[] {compName.toString()}));
        result.failed(smh.getLocalString
            ("com.sun.enterprise.tools.verifier.tests.connector.managed.ManagedConnectionFactoryTest.nonexist",
             "Error: The class [ {0} ] as defined in the managedconnecttionfactory-class deployment descriptor does not exist",
             new Object[] {managedConnectionFactoryImpl}));
      }            
      return mcf;
    }

    /**
     * <p>
     * Test wether the class declared in the deployemnt descriptor under the 
     * managedconnecttionfactory-class tag implements an interface 
     * </p>
     * 
     * @param descriptor the deployment descriptor
     * @param interfaceName the interface name we look for
     * @param result instance to use to put the result of the test
     * @return true if the test succeeds
     */
    protected boolean testImplementationOf(ConnectorDescriptor descriptor, String interfaceName, Result result) 
    {
      Class mcf = testManagedConnectionFactoryImpl(descriptor, result);
      if (mcf != null) 
        return testImplementationOf(mcf, interfaceName, result);
      return false;
    }
}
