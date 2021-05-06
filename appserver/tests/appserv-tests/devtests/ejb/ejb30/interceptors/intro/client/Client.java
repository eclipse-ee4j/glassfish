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

package com.sun.s1asdev.ejb.ejb30.interceptors.session.client;

import java.io.*;
import java.util.*;
import javax.naming.*;
import jakarta.ejb.EJB;
import com.sun.s1asdev.ejb.ejb30.interceptors.intro.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static @EJB Sless sless;

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("ejb-ejb30-interceptors-session");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-ejb30-interceptors-sessionID");
    }

    public Client (String[] args) {
    }

    public void doTest() {
        try {
            System.out.println("invoking sless");
            String result1 = sless.concatAndReverse("<One>", "<Two>");
            String result2 = sless.concatAndReverse("<One>", "Null");
            String result3 = sless.concatAndReverse("nuLL", "Null");
            System.out.println("Got : " + result1);
            System.out.println("Got : " + result2);
            System.out.println("Got : " + result3);

            System.out.println("Got : " + sless.plus((byte) 2, (short) 3, 4));
            System.out.println("Got : " + sless.isGreaterShort(new Short((short) 5), new Long(7)));
            stat.addStatus("local test1" , stat.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local test1" , stat.FAIL);
        }
    }

}

