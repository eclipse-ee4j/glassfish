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

package com.sun.s1asdev.jdbc.datasource40.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;

import com.sun.s1asdev.jdbc.datasource40.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.datasource40.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SimpleBMPClient {

    public static void main(String[] args)
            throws Exception {

        SimpleReporterAdapter stat = new SimpleReporterAdapter();
        String testSuite = "jdbcsimple ";

        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
        SimpleBMPHome simpleBMPHome = (SimpleBMPHome)
                javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

        SimpleBMP simpleBMP = simpleBMPHome.create();
        stat.addDescription("Running Datasource40 tests");
        if (simpleBMP.test1()) {
            stat.addStatus(testSuite + "test1 : ", stat.PASS);
        } else {
            stat.addStatus(testSuite + "test1 : ", stat.FAIL);
        }

        if (simpleBMP.test2()) {
            stat.addStatus(testSuite + "test2 : ", stat.PASS);
        } else {
            stat.addStatus(testSuite + "test2 : ", stat.FAIL);
        }

        if (simpleBMP.test3()) {
            stat.addStatus(testSuite + "test3 : ", stat.PASS);
        } else {
            stat.addStatus(testSuite + "test3 : ", stat.FAIL);
        }

        stat.printSummary();


    }
}
