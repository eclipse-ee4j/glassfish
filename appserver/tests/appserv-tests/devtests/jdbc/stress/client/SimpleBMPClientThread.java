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

package com.sun.s1asdev.jdbc.stress.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;

import com.sun.s1asdev.jdbc.stress.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.stress.ejb.SimpleBMP;

public class SimpleBMPClientThread extends Thread {
    InitialContext ic;
    SimpleBMP simpleBMP;
    public boolean runFlag = true;
    int id_;

    public SimpleBMPClientThread( int id ) throws Exception  {
        id_ = id;
        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
        SimpleBMPHome simpleBMPHome = (SimpleBMPHome)
        javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);
        simpleBMP = simpleBMPHome.create();
    }

    public void run() {
        int numRan = 0;
        int numPassed = 0;
        System.out.println("Thread : " +id_ + " running");
        while( runFlag == true ) {
            numRan++;
            try {
                if ( simpleBMP.test1( id_) ) {
                    numPassed++;
                }
            } catch( Exception e) {
                System.out.println( "Failed to run after : " + numPassed );
                break;
            }
        }

        System.out.println("Thread : "+id_ + " ran : " + numRan +
            " passed: " + numPassed );
    }
}
