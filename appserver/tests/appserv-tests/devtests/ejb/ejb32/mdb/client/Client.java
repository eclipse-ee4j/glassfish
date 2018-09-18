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

package com.sun.s1asdev.ejb.ejb32.mdb.client;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.s1asdev.ejb.ejb32.mdb.ejb.ResultsRemote;

import javax.naming.InitialContext;
import java.util.List;

/**
 * Modern MDB test
 *
 * Verifies that the resource adapter:
 *  - has access to the beanClass via the activation spec
 *  - can obtain a LocalBean-like view
 *
 * @author David Blevins
 */
public class Client {
    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");

    public static void main(String args[]) {
        stat.addDescription("ejb32-mdb");

        try {
            ResultsRemote resultsRemote = (ResultsRemote) new InitialContext().lookup("java:global/ejb32-mdb/ejb32-mdb-ejb/ResultsBean!com.sun.s1asdev.ejb.ejb32.mdb.ejb.ResultsRemote");

//            System.out.println("awaitInvocations - start");
            assertStatus("ejb32-mdb: awaitInvocations", resultsRemote.awaitInvocations());

            final List<String> invoked = resultsRemote.getInvoked();
            assertStatus("ejb32-mdb: method one", invoked.contains("one - intercepted"));
            assertStatus("ejb32-mdb: method two", invoked.contains("two - intercepted"));
            assertStatus("ejb32-mdb: method three", invoked.contains("three - intercepted"));
            assertStatus("ejb32-mdb: total invocations", invoked.size() == 3);

        } catch (Exception e) {
            stat.addStatus("ejb32-mdb: ", stat.FAIL);
            e.printStackTrace();
        }

        stat.printSummary("ejb32-mdb");
    }

    private static void assertStatus(final String message, final boolean condition) {
        System.out.println(message + " : " + condition);
        stat.addStatus(message, condition ? stat.PASS : stat.FAIL);
    }
}
