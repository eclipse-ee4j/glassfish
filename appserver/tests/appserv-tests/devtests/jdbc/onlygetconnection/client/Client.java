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

package com.sun.s1asdev.jdbc.onlygetconnection.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;

import com.sun.s1asdev.jdbc.onlygetconnection.ejb.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private SimpleReporterAdapter stat = new SimpleReporterAdapter();
    private String testSuite = "jdbc-onlygetconnection ";
    private NoTxConnTest bean;


    public static void main(String[] args)
        throws Exception {

//         SimpleReporterAdapter stat = new SimpleReporterAdapter();
//
//        InitialContext ic = new InitialContext();
//        Object objRef = ic.lookup("java:comp/env/ejb/NoTxConnTestEJB");
//        NoTxConnTestHome home = (NoTxConnTestHome)
//            javax.rmi.PortableRemoteObject.narrow(objRef, NoTxConnTestHome.class);
//
//        NoTxConnTest bean = home.create();

//      if ( bean.test1() ) {
//            stat.addStatus(testSuite+" test1 : ", stat.PASS);
//        } else {
//            stat.addStatus(testSuite+" test1 : ", stat.FAIL);
//        }
//        stat.printSummary();

        if ( args.length == 0 ) {
            (new Client()).runSingleThreaded();
        } else {
            int numRuns = 10;
            try {
                numRuns = Integer.parseInt( args[0] );
            } catch(NumberFormatException e) {
            } finally {
                (new Client()).runMultiThreaded( numRuns );
            }
        }

    }

    public Client() throws Exception {
        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/NoTxConnTestEJB");
        NoTxConnTestHome home = (NoTxConnTestHome)
            javax.rmi.PortableRemoteObject.narrow(objRef, NoTxConnTestHome.class);

        bean = home.create();
    }

    public void runSingleThreaded() throws Exception {
        if ( bean.test1() ) {
            stat.addStatus(testSuite+" test1 : ", stat.PASS);
        } else {
            stat.addStatus(testSuite+" test1 : ", stat.FAIL);
        }
        stat.printSummary();

    }


    public void runMultiThreaded( int numThreads ) throws Exception {
        MTClient[] mtc = new MTClient[ numThreads ];
        boolean result = true;
        int failCount = 0;

        for ( int i = 0; i < numThreads; i++) {
            mtc[i] = new MTClient( i );
            mtc[i].start();
        }

        //Wait for all threads to end before printing summary
        for ( int i = 0; i < numThreads; i++ ) {
            mtc[i].join();
        }

        for ( int i = 0; i < numThreads; i++ ) {
            result &= mtc[i].result;
            if ( !mtc[i].result ) {
                failCount++;
            }

        }

        System.out.println("Total Threads: " + numThreads );
        System.out.println("Total Passed : " + (numThreads - failCount ) );
        System.out.println("Total Failed : " + failCount );

        if ( result ) {
            stat.addStatus( testSuite + " multithreaded-test : " , stat.PASS );
        } else {
            stat.addStatus( testSuite + " multithreaded-test : " , stat.FAIL );
        }

        stat.printSummary();
    }

    class MTClient extends Thread {
        int id_;
        boolean result = false;

        MTClient( int id ) {
            id_ = id;
        }

        public void run() {
            try {
                result = bean.test1();
            } catch( Exception e ) {
            }
        }
    }
}
