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

package com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.use_nonjtadatasource.client;

import java.util.Map;
import java.util.Map.Entry;

import jakarta.ejb.EJB;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.use_nonjtadatasource.ejb.Tester;

public class Client {

    private static SimpleReporterAdapter stat = new SimpleReporterAdapter(
            "appserv-tests");
    @EJB(beanName = "SlessBean")
    private static Tester slessBean;

    public static void main(String[] args) {
        stat.addDescription("ejb32-persistence-unsynchronizedPC-use-nonjtadatasource");
        Client client = new Client();
        client.doTest();
        stat.printSummary("ejb32-persistence-unsynchronizedPC-use-nonjtadatasource");
    }

    public void doTest() {
        System.out.println("I am in client");
        try {
            Map<String, Boolean> resultMap = slessBean.doTest();
            for (Entry<String, Boolean> entry : resultMap.entrySet()) {
                stat.addStatus(entry.getKey(), entry.getValue() ? stat.PASS : stat.FAIL);
            }
        } catch (Exception e) {
            e.printStackTrace();
            stat.addStatus("local main", stat.FAIL);
        }
    }

}
