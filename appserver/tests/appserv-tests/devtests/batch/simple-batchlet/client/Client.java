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

package com.sun.ejb.devtest.client;

import java.io.*;
import java.util.*;
import javax.naming.*;
import jakarta.ejb.EJB;
import com.oracle.javaee7.samples.batch.simple.batchlet.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

@EJB(name="ejb/GG", beanInterface=Sless.class)
public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("batch-pay-rool-job-ejb-stateless");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("batch-pay-rool-job-ejb-stateless");
    }

    public Client (String[] args) {
    }

    private static @EJB(name="ejb/kk") Sless sless;

    public void doTest() {
        try {
            (new InitialContext()).lookup("java:comp/env/ejb/GG");
            long executionId = sless.submitJob();
            System.out.println("************************************************");
            System.out.println("******* JobID: " + executionId + " ******************");
            System.out.println("************************************************");
            String jobBatchStatus = "";
            for (int sec=10; sec>0; sec--) {
                try {
                    jobBatchStatus = sless.getJobExitStatus(executionId);
                    if (! "COMPLETED".equals(jobBatchStatus)) {
                        System.out.println("Will sleep for " + sec + " more seconds...: " + jobBatchStatus);
                        Thread.currentThread().sleep(1000);
                    }
                } catch (Exception ex) {
                }
            }
            stat.addStatus("simple-batchlet payroll", ("COMPLETED".equals(jobBatchStatus) ? stat.PASS : stat.FAIL));
        } catch (Exception ex) {
            stat.addStatus("simple-batchlet payroll", stat.FAIL);
        }
    }

}

