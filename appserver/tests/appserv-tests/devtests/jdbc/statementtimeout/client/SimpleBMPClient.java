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

package com.sun.s1asdev.jdbc.statementtimeout.client;

import javax.naming.*;

import com.sun.s1asdev.jdbc.statementtimeout.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.statementtimeout.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SimpleBMPClient {

    public static void main(String[] args)
            throws Exception {

        SimpleReporterAdapter stat = new SimpleReporterAdapter();
        String testSuite = "StatementTimeout ";

        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
        SimpleBMPHome simpleBMPHome = (SimpleBMPHome)
                javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

        SimpleBMP simpleBMP = simpleBMPHome.create();
        stat.addDescription("JDBC Statement Timeout Tests");

        if (simpleBMP.statementTest()) {
            stat.addStatus(testSuite + " statementTest : ", SimpleReporterAdapter.PASS);
        } else {
            stat.addStatus(testSuite + " statementTest : ", SimpleReporterAdapter.FAIL);
        }

        if (simpleBMP.preparedStatementTest()) {
            stat.addStatus(testSuite + " preparedStatementTest : ", SimpleReporterAdapter.PASS);
        } else {
            stat.addStatus(testSuite + " preparedStatementTest : ", SimpleReporterAdapter.FAIL);
        }

        if (simpleBMP.callableStatementTest()) {
            stat.addStatus(testSuite + " callableStatementTest : ", SimpleReporterAdapter.PASS);
        } else {
            stat.addStatus(testSuite + " callableStatementTest : ", SimpleReporterAdapter.FAIL);
        }

        stat.printSummary();
    }
}
