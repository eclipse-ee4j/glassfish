/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package eetimer;

import java.util.Map;

public class ListTimersTest extends TimerTestBase {

    public static void main(String[] args) {
        (new ListTimersTest()).runTests();
    }

    @Override
    protected String getTestDescription() {
        return "devtests for list-timers";
    }

    public void runTests() {
        try {
            deployEjbCreateTimers(cluster_name);
            listTimersCluster();
            listTimers();
            listTimersInstance3Empty();
        } finally {
            undeployEjb(cluster_name);
        }

        try {
            deployEjbCreateTimers(instance_name_3);
            listTimersInstance3();
        } finally {
            undeployEjb(instance_name_3);
        }
        stat.printSummary();
    }

    public void listTimers() {
        String testName = "listTimers";
        AsadminReturn output = asadminWithOutput("list-timers");
        Map<String, Integer> timerCounts = countInstanceTimers(output.out);
        report(testName, timerCounts.get("server") == 0);
    }

    //standalone instance
    public void listTimersInstance3Empty() {
        String testName = "listTimersInstance3Empty";
        AsadminReturn output = asadminWithOutput("list-timers", instance_name_3);
        Map<String, Integer> timerCounts = countInstanceTimers(output.out);
        report(testName, timerCounts.get(instance_name_3) == 0);
    }

    public void listTimersInstance3() {
        String testName = "listTimersInstance3";
        AsadminReturn output = asadminWithOutput("list-timers", instance_name_3);
        Map<String, Integer> timerCounts = countInstanceTimers(output.out);
        report(testName, timerCounts.get(instance_name_3) == 1);
    }

    public void listTimersCluster() {
        String testName = "listTimersCluster";
        AsadminReturn output = asadminWithOutput("list-timers", cluster_name);
        Map<String, Integer> timerCounts = countInstanceTimers(output.out);
        report(testName, timerCounts.get(instance_name_1) == 1);
        report(testName, timerCounts.get(instance_name_2) == 1);
    }
}
