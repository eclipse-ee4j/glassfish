/*
 * Copyright (c) 2002, 2018 Oracle and/or its affiliates. All rights reserved.
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

package jaxr;


import java.io.*;
import java.util.*;
import jakarta.ejb.EJBHome;
import jaxr.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import org.omg.CORBA.ORB;

public class JaxrClient {

    String company = "Sun";
    String url = null;
    String ctxFactory = null;
    String jndiName = null;
    public static void main (String[] args) {
        JaxrClient client = new JaxrClient(args);
        client.doTest();
    }

    public JaxrClient (String[] args) {
             if (args.length == 3) {
                url = args[0];
                ctxFactory = args[1];
                jndiName = args[2];
            }
    }

    public String doTest() {

        String res = "fail";

        try {
                Properties env = new Properties();
                env.put("java.naming.provider.url", url);
                env.put("java.naming.factory.initial", ctxFactory);
                // Initialize the Context with JNDI specific properties
                InitialContext context = new InitialContext(env);
                System.out.println("Context Initialized with " +
                                   "URL: " + url + ", Factory: " + ctxFactory);
                // Create Home object
                System.out.println("*****"+jndiName);
                java.lang.Object obj = context.lookup(jndiName);
                // create EJB using factory from container
                //java.lang.Object objref = ic.lookup("MyJaxr");

                System.out.println("Looked up home!!");

                JaxrHome  home = (JaxrHome)PortableRemoteObject.narrow(
                                 obj, JaxrHome.class);
                System.out.println("Narrowed home!!");

                JaxrRemote hr = home.create();
                System.out.println("Got the EJB!!");

                // invoke method on the EJB
                System.out.println (" Looking up company information for "+company);
                System.out.println(hr.getCompanyInformation(company));
                hr.remove();
    } catch(NamingException ne){
            System.out.println("Caught exception while initializing context.\n");
            ne.printStackTrace();
        System.out.println (" Test Failed !");
            return res;
    } catch(Exception re) {
            re.printStackTrace();
        System.out.println (" Test Failed !");
            return res;
    }
        res = "pass";
    System.out.println (" Test Passed !");
        return res;

    }

}

