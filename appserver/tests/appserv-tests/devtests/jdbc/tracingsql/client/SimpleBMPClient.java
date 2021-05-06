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

package com.sun.s1asdev.jdbc.tracingsql.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;

import com.sun.s1asdev.jdbc.tracingsql.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.tracingsql.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SimpleBMPClient {

    public static void main(String[] args)
        throws Exception {

         SimpleReporterAdapter stat = new SimpleReporterAdapter();
        String testSuite = "SqlTracing ";

    InitialContext ic = new InitialContext();
    Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
        SimpleBMPHome simpleBMPHome = (SimpleBMPHome)
    javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

    SimpleBMP simpleBMP = simpleBMPHome.create();
    stat.addDescription("JDBC Sql Tracing Tests");

    /*if ( simpleBMP.statementTest() ) {
            stat.addStatus(testSuite+" statementTest : ", stat.PASS);
        } else {
            stat.addStatus(testSuite+" statementTest : ", stat.FAIL);
        }*/

        if ( simpleBMP.preparedStatementTest() ) {
            stat.addStatus(testSuite+" preparedStatementTest : ", stat.PASS);
        } else {
            stat.addStatus(testSuite+" preparedStatementTest : ", stat.FAIL);
        }

        if( simpleBMP.compareRecords()) {
            stat.addStatus(testSuite+" Sql Tracing Test : ", stat.PASS);
        } else {
            stat.addStatus(testSuite+" Sql Tracing Test : ", stat.FAIL);
        }
    /*if ( simpleBMP.callableStatementTest() ) {
        stat.addStatus(testSuite+" callableStatementTest : ", stat.PASS);
    } else {
        stat.addStatus(testSuite+" callableStatementTest : ", stat.FAIL);
    }

    if ( simpleBMP.metaDataTest() ) {
        stat.addStatus(testSuite+" metaDataTest : ", stat.PASS);
    } else {
        stat.addStatus(testSuite+" metaDataTest : ", stat.FAIL);
    }

    if ( simpleBMP.resultSetTest() ) {
        stat.addStatus(testSuite+" resultSetTest : ", stat.PASS);
    } else {
        stat.addStatus(testSuite+" resultSetTest : ", stat.FAIL);
    }*/

    stat.printSummary();
    }
}
