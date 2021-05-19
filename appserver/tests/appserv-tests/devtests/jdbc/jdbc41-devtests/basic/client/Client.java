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

package com.sun.s1asdev.jdbc41.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;

import com.sun.s1asdev.jdbc41.ejb.SimpleSessionHome;
import com.sun.s1asdev.jdbc41.ejb.SimpleSession;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    public static void main(String[] args)
        throws Exception {

        SimpleReporterAdapter stat = new SimpleReporterAdapter();
        String testSuite = " JDBC41 tests on JDK7 ";

        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleSessionHome");
        SimpleSessionHome simpleSessionHome = (SimpleSessionHome)
        javax.rmi.PortableRemoteObject.narrow(objRef, SimpleSessionHome.class);

        stat.addDescription("Running JDBC41 testSuite ");
        SimpleSession simpleSession = simpleSessionHome.create();
        if (simpleSession.test1() ) {
            stat.addStatus( testSuite + " test1 : Connection API/abort Test : " , stat.PASS );
        } else {
            stat.addStatus( testSuite + " test1 : Connection API/abort Test : " , stat.FAIL );
        }

        if (simpleSession.test2() ) {
            stat.addStatus( testSuite + " test2 : Connection API/abort usertx Test : " , stat.PASS );
        } else {
            stat.addStatus( testSuite + " test2 : Connection API/abort usertx Test : " , stat.FAIL );
        }

        if (simpleSession.test3() ) {
            stat.addStatus( testSuite + " test3 : DatabaseMetaData Test : " , stat.PASS );
        } else {
            stat.addStatus( testSuite + " test3 : DatabaseMetaData Test : " , stat.FAIL );
        }

        if (simpleSession.test4() ) {
            stat.addStatus( testSuite + " test4 : Statement API/Connection abort usertx Test : " , stat.PASS );
        } else {
            stat.addStatus( testSuite + " test4 : Statement API/Connection abort usertx Test : " , stat.FAIL );
        }
        stat.printSummary();
    }
}
