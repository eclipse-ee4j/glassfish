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

package com.sun.s1asdev.jdbc.simplemysql.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;

import com.sun.s1asdev.jdbc.simplemysql.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.simplemysql.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SimpleBMPClient {

    public static void main(String[] args)
        throws Exception {

         SimpleReporterAdapter stat = new SimpleReporterAdapter();
        String testSuite = "jdbcsimplemysql ";

        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
        SimpleBMPHome simplemysqlBMPHome = (SimpleBMPHome)
        javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

        SimpleBMP simplemysqlBMP = simplemysqlBMPHome.create();

        if ( simplemysqlBMP.test1(10) ) {
            stat.addStatus(testSuite+"test1 : ", stat.PASS);
        } else {
            stat.addStatus(testSuite+"test1 : ", stat.FAIL);
        }

        if ( simplemysqlBMP.test2() ) {
            stat.addStatus(testSuite+"test2 : ", stat.PASS);
        } else {
            stat.addStatus(testSuite+"test2 : ", stat.FAIL);
        }

        if ( simplemysqlBMP.test3() ) {
            stat.addStatus(testSuite+"test3 : ", stat.PASS);
        } else {
            stat.addStatus(testSuite+"test3 : ", stat.FAIL);
        }

        System.out.println("jdbc simplemysql status: ");
        stat.printSummary();


    }
}
