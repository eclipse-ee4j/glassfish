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

package com.sun.s1asdev.jdbc.customval.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;

import com.sun.s1asdev.jdbc.customval.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.customval.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    public static void main(String[] args)
            throws Exception {

        SimpleReporterAdapter stat = new
                SimpleReporterAdapter();
        String testSuite = "CustomValidation-1 ";
        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
        SimpleBMPHome convalBMPHome = (SimpleBMPHome)
                javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

        SimpleBMP convalBMP = convalBMPHome.create();
        stat.addDescription("Custom Validation Test using a custom validator ");

        if (args != null && args.length > 0) {
            String param = args[0];

            switch (Integer.parseInt(param)) {
                case 1: {
                    if (convalBMP.test1()) {
                        stat.addStatus(testSuite + "test-1 ", stat.PASS);
                    } else {
                        stat.addStatus(testSuite + "test-1 ", stat.FAIL);
                    }
                    break;
                }
                case 3: {
                    if (convalBMP.test1()) {
                        stat.addStatus(testSuite + "test-3 ", stat.PASS);
                        System.out.println("test-3 returned true as validation is enabled ");
                    } else {
                        stat.addStatus(testSuite + "test-3 ", stat.FAIL);
                    }
                    break;
                }
                case 4: {
                    if (convalBMP.test1()) {
                        stat.addStatus(testSuite + "test-4 ", stat.PASS);
                        System.out.println("test-4 returned true as validation is enabled ");
                    } else {
                        stat.addStatus(testSuite + "test-4 ", stat.FAIL);
                    }
                    break;
                }

                case 2: {
                    try {
                        if(convalBMP.test1()){
                            stat.addStatus(testSuite + "test-2 ", stat.FAIL);
                        }else{
                            stat.addStatus(testSuite + "test-2 ", stat.PASS);
                            System.out.println("test-2 returned false as validation is not enabled ");
                        }
                    }
                    catch (Exception e) {
                        stat.addStatus(testSuite + "test1 ", stat.PASS);
                    }
                    break;
                }
            }
            stat.printSummary();
        }
    }
}
