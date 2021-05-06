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

package com.sun.s1peqe.security.authoriz.methodperms;

import java.io.*;
import java.util.*;
import jakarta.ejb.EJBHome;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import org.omg.CORBA.ORB;
import com.sun.enterprise.security.LoginContext;
import com.sun.enterprise.security.LoginException;
import java.rmi.RemoteException;
import java.security.*;

import com.sun.ejte.ccl.reporter.*;

public class MethodPermClient {
    private static SimpleReporterAdapter stat=new SimpleReporterAdapter("appserv-tests");
    public static void main (String[] args) {

        MethodPermClient client = new MethodPermClient(args);
        System.out.print("-->EJB method permissions!");
        stat.addDescription("EJB method permissions");
        client.doTest();
        stat.printSummary("Authorize_methodperms");

    }

    public MethodPermClient (String[] args) {
        //super(args);
    }

    public String doTest() {

        MethodPermRemote hr=null;
        String res=null;
        Context ic = null;
        LoginContext lc=null;
        MethodPermRemoteHome home=null;
        try{
            ic = new InitialContext();
//        Security.setProperty("policy.allowSystemProperty", "true");
            lc = new LoginContext();
            lc.login("j2ee","j2ee");
        // create EJB using factory from container
            java.lang.Object objref = ic.lookup("MyMethodPerm");

            System.err.println("Looked up home!!");

            home = (MethodPermRemoteHome)PortableRemoteObject.narrow(
                                           objref, MethodPermRemoteHome.class);
            System.err.println("Narrowed home!!");

            hr = home.create(helloStr);
            System.err.println("Got the EJB!!");
        }catch (Exception ex) {
            ex.printStackTrace();
            //res = Tester.kTestFailed;
            stat.addStatus("Sec::Authorize_methodperms Testsuite",stat.FAIL);
            res="FAIL";
        }
        // invoke 3 overloaded methods on the EJB
        try{
            System.out.println ("Calling authorized method - authorizedMethod");
            System.out.println(hr.authorizedMethod());

            System.out.println ("Calling authorized method - authorizedMethod - hi 129");
            System.out.println(hr.authorizedMethod("Hi", 129));

            System.out.println ("Calling authorized method - authorizedMethod 115");
            System.out.println(hr.authorizedMethod(115));

            //res  = Tester.kTestPassed;
                stat.addStatus("Sec::Authorize_methodperms Test1-Calling authorized method",stat.PASS);

        } catch(Exception re){
            re.printStackTrace();
            System.out.println("Test Failed");
            stat.addStatus("Sec::Authorize_methodperms Test1-Calling authorized method",stat.FAIL);
            res="FAIL";
              //return Tester.kTestFailed;
        }
        try{
        // invoke unauthorized method on the EJB
            System.out.println ("Calling unauthorized method - sayGoodBye");
            System.out.println(hr.sayGoodbye());
            System.out.println (" Test failed: able to call good bye method!");
            //return Tester.kTestFailed;
            stat.addStatus("Sec::Authorize_methodperms Test2-Calling unauthorized method",stat.FAIL);
            res="FAIL";
        } catch(Exception gbye){
        //res = Tester.kTestPassed;
            stat.addStatus("Sec::Authorize_methodperms Test2-Calling unauthorized method",stat.PASS);
            res="PASS";
        }

        try{
            // invoke method on the EJB not authorized
            System.out.println ("Calling unauthorized method - unauthorizedMethod");
            hr.unauthorizedMethod();
           //res  = Tester.kTestFailed;
            stat.addStatus("Sec::Authorize_methodperms Test3-expected Exception-Calling unauthorized method",stat.FAIL);
            res="FAIL";
        } catch (RemoteException remex) {
            System.out.println("Caught expected RemoteException from unauthorizedMethod()");
            //res  = Tester.kTestPassed;
            stat.addStatus("Sec::Authorize_methodperms Test3-expected Exception-Calling unauthorized method",stat.PASS);
            res="PASS";
        } catch(Exception ex){
            stat.addStatus("Sec::Authorize_methodperms Test3-expected Exception-Calling unauthorized method",stat.FAIL);
            res="FAIL";
        }
        return res;

    }


    public final static String helloStr = "Hello MethodPerm!!!";
}

