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

package client.methodintf;

import jakarta.ejb.*;
import javax.naming.*;

import ejb32.methodintf.St;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    InitialContext context;

    public static void main(String args[]) {
        stat.addDescription("ejb32-methodintf");

        try {
            St stles = (St) new InitialContext().lookup("java:global/ejb32-methodintf-ejb/StlesEJB");
            St stful = (St) new InitialContext().lookup("java:global/ejb32-methodintf-ejb/StfulEJB");
            stles.test();
            stful.test();
            System.out.println("Waiting timer to expire to verify the results");
            Thread.sleep(3000);
            boolean pass = stles.verify() && stful.verify();
            stat.addStatus("ejb32-methodintf: ", ((pass)? stat.PASS : stat.FAIL) );

        } catch(Exception e) {
            stat.addStatus("ejb32-methodintf: ", stat.FAIL);
            e.printStackTrace();
        }

        stat.printSummary("ejb32-methodintf");
    }

}
