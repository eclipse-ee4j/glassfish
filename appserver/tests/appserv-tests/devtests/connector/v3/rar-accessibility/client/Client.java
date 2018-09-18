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

package com.sun.s1asdev.connector.rar_accessibility_test.client;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.s1asdev.connector.rar_accessibility_test.ejb.SimpleSession;
import com.sun.s1asdev.connector.rar_accessibility_test.ejb.SimpleSessionHome;

import javax.naming.InitialContext;

public class Client {

    public static void main(String[] args)
            throws Exception {

        int expectedCount = Integer.parseInt(args[0]);
        SimpleReporterAdapter stat = new SimpleReporterAdapter();
        String testSuite = "rar-accesibility";

        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleSessionHome");
        SimpleSessionHome simpleSessionHome = (SimpleSessionHome)
                javax.rmi.PortableRemoteObject.narrow(objRef, SimpleSessionHome.class);

        stat.addDescription("Running rar_accessibility connector test ");
        SimpleSession bean = simpleSessionHome.create();

            try{
                if(bean.test1(expectedCount)){
                    stat.addStatus(testSuite + " test :  ", stat.PASS);
                }else{
                    stat.addStatus(testSuite + " test :  ", stat.FAIL);
                }
            }catch(Exception e){
                e.printStackTrace();
            }

        stat.printSummary();
    }
}
