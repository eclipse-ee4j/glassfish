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

package com.sun.s1asdev.ejb.ejb30.hello.session2full.client;

import java.io.*;
import java.util.*;
import jakarta.ejb.NoSuchEJBException;
import javax.naming.InitialContext;
import javax.naming.Context;
import com.sun.s1asdev.ejb.ejb30.hello.session2full.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client extends ClientSuper {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("ejb-ejb30-hello-session2full");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-ejb30-hello-session2fullID");
    }

    private static Sful sful1;
    private static Sful sful2;
    private static Sful sful3;
    private static Sful sful4;

    private static Sless sless1;

    // linked to SlessEJB2 via sun-application-client.xml
    // so no beanName disambiguation is needed
    private static Sless sless2;

    // Only one target bean with Remote intf SlessSub so no linking info
    // necessary
    private static SlessSub sless3;

    public Client (String[] args) {
    }

    public void doTest() {

        try {

            System.out.println("Calling superSless1");
            superSless1.hello();
            System.out.println("Called superSless1");

            sful1.hello();

            sful2.hello();

            sful1.set("1");
            sful2.set("2");

            String get1 = sful1.get();
            String get2 = sful2.get();
            System.out.println("get1 =" + get1);
            System.out.println("get2 =" + get2);

            if( get1.equals(get2) ) {
                throw new Exception("SFSB get test failed");
            }

            // Call application-defined @Remove method. This method has
            // no relationship to EJBObject.remove().  It's a coincidence
            // that it has the same name.
            sful1.remove();
            boolean passed = true;
            try {
                sful1.hello();
                passed = false;
            } catch(NoSuchEJBException e) {
                System.out.println("Successfully got exception after " +
                                   "attempting to access implicitly removed" +
                                   " sfsb");
            }
            if( !passed ) {
                throw new Exception("Didn't get expected sfsb access exception");
            }

            // Force @Remove method to throw an exception, but bean should
            // not be removed because retainIfException=true
            passed = true;
            try {
                sful2.removeRetainIfException(true);
                passed = false;
            } catch(Exception e) {
                System.out.println("Successfully got application exception " +
                                   " from remote SFSB @Remove method");
            }
            if( !passed ) {
                throw new Exception("Didn't get expected sfsb access exception");
            }


            // Now call it again but don't have it throw an exception.  In
            // this case, the bean should be removed.
            sful2.removeRetainIfException(false);

            passed = true;
            try {
                sful2.hello();
                passed = false;
            } catch(NoSuchEJBException e) {
                System.out.println("Successfully got exception after " +
                                   "attempting to access implicitly removed" +
                                   " sfsb");
            }

            if( !passed ) {
                throw new Exception("Didn't get expected sfsb access exception");
            }


            // Call an @Remove method for which retainIfException=false and
            // make it throw an exception.  The bean should still be removed.
            passed = true;
            try {
                sful3.removeNotRetainIfException(true);
                passed = false;
            } catch(Exception e) {
                System.out.println("Got expected exception from @Remove mthd");
            }

            if( !passed ) {
                throw new Exception("Didn't get expected sfsb remove exception");
            }

            passed = true;
            try {
                sful3.hello();
                passed = false;
            } catch(NoSuchEJBException e) {
                System.out.println("Successfully got exception after " +
                                   "attempting to access implicitly removed" +
                                   " sfsb");
            }
            if( !passed ) {
                throw new Exception("Didn't get expected sfsb access exception");
            }

            // Call an @Remove method for which retainIfException=false and
            // don't have it throw an exception.  The bean should be removed.
            passed = true;
            sful4.removeNotRetainIfException(false);
            try {
                sful4.hello();
                passed = false;
            } catch(NoSuchEJBException e) {
                System.out.println("Successfully got exception after " +
                                   "attempting to access implicitly removed" +
                                   " sfsb");
            }
            if( !passed ) {
                throw new Exception("Didn't get expected sfsb access exception");
            }


            sless1.hello();
            Sless r1 = sless1.roundTrip(sless1);
            sless1.roundTrip(sless2);

            sless2.hello();
            sless2.roundTrip(sless1);
            Sless r2 = sless2.roundTrip(sless2);

            try {
                sless1.hello2();
                throw new Exception("Did not receive CreateException");
            } catch(jakarta.ejb.CreateException ce) {
                System.out.println("Successfully caught app exception");
            }

            if( sless1.getId().equals(sless2.getId()) ) {
                throw new Exception("getId() test failed");
            }

            if( r1.getId().equals(r2.getId()) ) {
                throw new Exception("remote param passing getId() test failed");
            }

            sless3.hello();
            sless3.hello3();


            /*
            Context ic = new InitialContext();

            Sless typeLevelSless1 = (Sless)
                ic.lookup("java:comp/env/ejb/TypeLevelSless1");
            Sless typeLevelSless2 = (Sless)
                ic.lookup("java:comp/env/ejb/TypeLevelSless2");
            SlessSub typeLevelSless3 = (SlessSub)
                ic.lookup("java:comp/env/ejb/TypeLevelSless3");
            typeLevelSless1.hello();
            typeLevelSless2.hello();
            typeLevelSless3.hello();
            if( typeLevelSless1.getId().equals(typeLevelSless2.getId()) ) {
                throw new Exception("type-level@EJB getId() test failed");
            }
            System.out.println("Finished Type-level @EJB checks");
            */

            System.out.println("test complete");

            stat.addStatus("local main", stat.PASS);

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local main" , stat.FAIL);
        }

            return;
    }

}

