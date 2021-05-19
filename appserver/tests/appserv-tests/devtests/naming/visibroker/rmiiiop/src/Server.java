/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

// Server.java
import org.omg.PortableServer.*;

public class Server {

  public static void main(String[] args) {
    try {
      // Initialize the ORB.
      org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args,null);
      // get a reference to the root POA
      POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

      // Create policies for our persistent POA
      org.omg.CORBA.Policy[] policies = {
        rootPOA.create_lifespan_policy(LifespanPolicyValue.PERSISTENT)
      };
      // Create myPOA with the right policies
      POA myPOA = rootPOA.create_POA( "rmi_bank_poa", rootPOA.the_POAManager(),
                                        policies );
      // Create the servant
      AccountManagerImpl managerServant = new AccountManagerImpl();
      // Decide on the ID for the servant
      byte[] managerId = "RMIBankManager".getBytes();
      // Activate the servant with the ID on myPOA
      myPOA.activate_object_with_id(managerId, managerServant);

      // Activate the POA manager
      rootPOA.the_POAManager().activate();

      System.out.println(myPOA.servant_to_reference(managerServant) +
                         " is ready.");
      // Wait for incoming requests
      orb.run();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

}

