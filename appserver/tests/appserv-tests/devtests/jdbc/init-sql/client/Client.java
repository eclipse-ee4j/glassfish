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

package com.sun.s1asdev.jdbc.initsql.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;
import javax.sql.DataSource;
import com.sun.s1asdev.jdbc.initsql.ejb.SimpleSessionHome;
import com.sun.s1asdev.jdbc.initsql.ejb.SimpleSession;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = new SimpleReporterAdapter();
    private static String testSuite = "initsql-test";

    private static InitialContext ic;
    public static void main(String[] args)
        throws Exception {

        try {
            ic = new InitialContext();
        } catch(NamingException ex) {
            ex.printStackTrace();
        }

        Object objRef = ic.lookup("java:comp/env/ejb/SimpleSessionHome");
        SimpleSessionHome simpleSessionHome = (SimpleSessionHome)
        javax.rmi.PortableRemoteObject.narrow(objRef, SimpleSessionHome.class);
        stat.addDescription("Running initsql testSuite ");
        SimpleSession simpleSession = simpleSessionHome.create();

        if (args != null && args.length > 0) {
            String param = args[0];

            switch (Integer.parseInt(param)) {
                case 1: {
                    if (simpleSession.test1(false)) { //Case sensitive test
                        stat.addStatus(testSuite + "test-1 ", stat.PASS);
                    } else {
                        stat.addStatus(testSuite + "test-1 ", stat.FAIL);
                    }
                    break;
                }
                case 2: {
                    if (simpleSession.test1(true)) { //Case insensitivity test
                        stat.addStatus(testSuite + "test-2 ", stat.PASS);
                    } else {
                        stat.addStatus(testSuite + "test-2 ", stat.FAIL);
                    }
                    break;
                }
            }
        }

        stat.printSummary();
    }
}
