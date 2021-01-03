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

import HelloApp.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import java.util.*;
import javax.naming.*;
import javax.rmi.*;

public class HelloClient
{
  static Hello helloImpl;

  public static void main(String args[])
    {
      try{
	  Properties p1 = new Properties();
	  //p1.put( "org.omg.CORBA.ORBInitialHost", "localhost" );
	  //p1.put( "org.omg.CORBA.ORBInitialPort", "2510" );
	  p1.put("org.omg.CORBA.ORBClass", "com.inprise.vbroker.orb.ORB");
	  p1.put("org.omg.CORBA.ORBSingletonClass", "com.inprise.vbroker.orb.ORBSingleton");
	  org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init( new String[]{"",""}, p1 );
	  
	  // get the root naming context
	  org.omg.CORBA.Object objRef = 
	    orb.resolve_initial_references("NameService");
	  // Use NamingContextExt instead of NamingContext. This is 
	  // part of the Interoperable naming Service.  
	  NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
	  
	  // resolve the Object Reference in Naming
	  String name = "Hello";
	  helloImpl = HelloHelper.narrow(ncRef.resolve_str(name));
	  
	  System.out.println("Obtained a handle on server object: " + helloImpl);
	  System.out.println(helloImpl.sayHello());
	  helloImpl.shutdown();
	  
	  /*	  Properties p2 = new Properties();
	  p2.put( javax.naming.Context.INITIAL_CONTEXT_FACTORY, 
		  "org.glassfish.jndi.cosnaming.CNCtxFactory" );
	  p2.put("java.naming.corba.orb", orb);
	  
	  InitialContext ic = new InitialContext( p2);
	  
	  System.out.println("Done");
	  System.out.println("About to do lookup...");
	  
	  java.lang.Object o = ic.lookup("Hello" );
	  System.out.println("Completed lookup!!");
	  
	  HelloApp.Hello hello = (HelloApp.Hello) PortableRemoteObject.narrow( o, HelloApp.Hello.class );
	  
	  System.out.println(hello.sayHello());
	  System.out.println("Done");
	  
	  hello.shutdown();*/
	  

	} catch (Exception e) {
          System.out.println("ERROR : " + e) ;
	  e.printStackTrace(System.out);
	  }
    }

}
