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

package standaloneclient;

import java.util.*;
import jakarta.ejb.EJBHome;
import statelesshello.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import org.omg.CORBA.ORB;


public class HelloClient {

    public static void main(String[] args) {

        boolean testPositive = (Boolean.valueOf(args[0])).booleanValue();
        if(testPositive)
            System.out.println("Test expects successful result");
        else
            System.out.println("Test expected to fail");
        try {

            Context ic = new InitialContext();

            // create EJB using factory from container
            java.lang.Object objref = ic.lookup("MyStatelesshello");

            System.out.println("Looked up home!!");

            StatelesshelloHome home =
                (StatelesshelloHome) PortableRemoteObject.narrow(
                    objref,
                    StatelesshelloHome.class);
            System.out.println("Narrowed home!!");

            Statelesshello hr = home.create();
            System.out.println("Got the EJB!!");

            // invoke method on the EJB
            System.out.println(hr.sayStatelesshello());
            System.out.println(
                "Client's sayStatelesshello() method succeeded\n");
            try {

                System.out.println(
                    "Client now getting a User Defined Exception");
                System.out.println(hr.getUserDefinedException());

            } catch (StatelesshelloException he) {
                System.out.println("Success!  Caught StatelesshelloException");
                System.out.println(
                    "Client's getUserDefinedException() method succeeded\n");

            } catch (java.rmi.ServerException se) {
                if (se.detail instanceof StatelesshelloException) {
                    System.out.println(
                        "Success!  Caught StatelesshelloException");
                    System.out.println(
                        "Client's getUserDefinedException() method succeeded\n");

                } else {
                    System.out.println("Failure!  Caught unasked for Exception");
                    System.exit(-1);
                }
            }
            System.out.println(
                "Client is now trying to remove the session bean\n");
            hr.remove();
        } catch (NamingException ne) {
            if(testPositive) {
                System.out.println("Caught exception while initializing context : " +
                                   ne.getMessage() + " \n");
                System.exit(-1);
            } else {
                System.out.println("Recd exception as expected");
            }
        } catch (Exception re) {
            if(testPositive) {
                re.printStackTrace();
                System.out.println( "Session beans could not be removed by the client.\n");
                System.exit(-1);
            } else {
                System.out.println("Recd exception as expected");
            }
        }
        System.out.println(
            "Session bean was successfully removed by the client.\n");
    }
}
