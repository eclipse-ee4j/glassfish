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

package com.sun.s1asdev.jdbc.cpdsperf.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;

import com.sun.s1asdev.jdbc.cpdsperf.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.cpdsperf.ejb.SimpleBMP;

public class SimpleBMPClient {

    public static void main(String[] args)
        throws Exception {
        int numTimes = 100;
        if (args.length == 1) {
            numTimes = Integer.parseInt( args[0] );
        }

        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
        SimpleBMPHome simpleBMPHome = (SimpleBMPHome)
        javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

        SimpleBMP simpleBMP = simpleBMPHome.create( numTimes );

        long timeTaken = 0;
        System.out.println("----------------------------");
        System.out.println(" test1: Using ConnectionPoolDataSource");
        if ( (timeTaken = simpleBMP.test1()) != -1 ) {
            System.out.println("Passed: Num Connections: " + numTimes+
                "  Time taken :" + timeTaken);
        } else {
            System.out.println("Failed");
        }
        System.out.println("----------------------------");
        System.out.println();
        System.out.println("----------------------------");
        System.out.println(" test2: Using DataSource");
        if ( (timeTaken = simpleBMP.test2()) != -1 ) {
            System.out.println("Passed: Num Connections: " + numTimes+
                "  Time taken :" + timeTaken);
        } else {
            System.out.println("Failed");
        }

        System.out.println("----------------------------");
    }
}
