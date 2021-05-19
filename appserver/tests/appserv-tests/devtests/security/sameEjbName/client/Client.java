/*
 * Copyright (c) 2005, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.security.sameEjbName.client;

import jakarta.ejb.EJB;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");
    private static String testSuite = "Sec::SameEjbName test ";
    private static @EJB com.sun.s1asdev.security.sameEjbName.ejb.Hello hello1;
    private static @EJB com.sun.s1asdev.security.sameEjbName.ejb2.Hello hello2;

    public static void main(String[] args) {
        stat.addDescription("security-sameEjbName");
        String description = null;
        try {
            description = testSuite + " ejb1: rolesAllowed1";
            hello1.rolesAllowed1("Sun");
            stat.addStatus(description, stat.PASS);

            try {
                description = testSuite + " ejb1: rolesAllowed2";
                hello1.rolesAllowed2("Sun");
                stat.addStatus(description, stat.FAIL);
            } catch(Exception e) {
                System.out.println("Expected failure: " + e);
                stat.addStatus(description, stat.PASS);
            }

            try {
                description = testSuite + " ejb2: rolesAllowed1";
                hello2.rolesAllowed1("Java");
                stat.addStatus(description, stat.FAIL);
            } catch(Exception e) {
                System.out.println("Expected failure: " + e);
                stat.addStatus(description, stat.PASS);
            }

            description = testSuite + " ejb2: rolesAllowed2";
            hello2.rolesAllowed2("Java");
            stat.addStatus(description, stat.PASS);
        } catch(Exception ex) {
            ex.printStackTrace();
            stat.addStatus(description, stat.FAIL);
        }

        stat.printSummary("security-sameEjbName");
    }
}
