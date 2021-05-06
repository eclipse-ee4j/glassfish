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

package com.sun.s1asdev.ejb.bmp.simple.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;

import com.sun.s1asdev.ejb.bmp.simple.ejb.SimpleBMPHome;
import com.sun.s1asdev.ejb.bmp.simple.ejb.SimpleBMP;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SimpleBMPClient {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args)
        throws Exception
    {
        try {
            stat.addDescription("Testing bmp simple app.");
            InitialContext ic = new InitialContext();
            Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
            SimpleBMPHome simpleBMPHome = (SimpleBMPHome)
                javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

            int id= (int) System.currentTimeMillis();
             System.out.println("Starting test for id: " + id);
             SimpleBMP simpleBMP = simpleBMPHome.create(id);

            boolean threadPoolIDTestStatus =
                simpleBMP.isServicedBy("express-service-thread-pool");
            stat.addStatus("bmp ThreadPoolTest",
                ((threadPoolIDTestStatus == true) ?  stat.PASS : stat.FAIL));
        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("bmp simple", stat.FAIL);
        }
        stat.printSummary("simple");
    }
}
