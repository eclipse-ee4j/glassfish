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
import java.util.logging.Level;

public class MigrateTimersTest extends TimerTestBase {

    public static void main(String[] args) {
        (new MigrateTimersTest()).runTests();
    }

    @Override
    protected String getTestDescription() {
        return "devtests for migrate-timers";
    }

    public void runTests() {
        try {
            deployEjbCreateTimers(cluster_name);
            migrateTimers();
            migrateTimersWithTarget();
            migrateTimersOutsideCluster();
        } finally {
            //all associated timers will be removed upon undeploy, even if some
            //instances are offline.
            undeployEjb(cluster_name);
        }
        stat.printSummary();
    }

    //--target not specified, default to "server", should pick a running instance
    //from the same cluster
    public void migrateTimers() {
        String testName = "migrateTimers";

        //no automatic migration when stopping a instance since gms has been disabled
        asadmin("stop-instance", instance_name_1);
        AsadminReturn output = asadminWithOutput("migrate-timers", instance_name_1);
        logger.log(Level.INFO, "Finished migrate-timer: {0}", new Object[]{output.outAndErr});

        output = asadminWithOutput("list-timers", cluster_name);
        Map<String, Integer> timerCounts = countInstanceTimers(output.out);
        report(testName + instance_name_1 + "-0", timerCounts.get(instance_name_1) == 0);
        report(testName + instance_name_2 + "-2", timerCounts.get(instance_name_2) == 2);
    }

    public void migrateTimersWithTarget() {
        String testName = "migrateTimersWithTarget";

        //no automatic migration when stopping a instance since gms has been disabled
        asadmin("stop-instance", instance_name_2);
        asadmin("start-instance", instance_name_1);
        AsadminReturn output = asadminWithOutput("migrate-timers", "--target", instance_name_1 ,instance_name_2);
        logger.log(Level.INFO, "Finished migrate-timer: {0}", new Object[]{output.outAndErr});

        output = asadminWithOutput("list-timers", cluster_name);
        Map<String, Integer> timerCounts = countInstanceTimers(output.out);

        //3 timers in instance_1: 2 migrated from instance_2, 1 created after restart
        report(testName + instance_name_1 + "-3", timerCounts.get(instance_name_1) == 3);
        report(testName + instance_name_2 + "-0", timerCounts.get(instance_name_2) == 0);
    }

    public void migrateTimersOutsideCluster() {
        String testName = "migrateTimersOutsideCluster";

        //no automatic migration when stopping a instance since gms has been disabled
        asadmin("stop-instance", instance_name_1);
        asadmin("start-instance", instance_name_3);
        AsadminReturn output = asadminWithOutput("migrate-timers", "--target", instance_name_3, instance_name_1);
        logger.log(Level.INFO, "Finished migrate-timer: {0}", new Object[]{output.outAndErr});
        report(testName, output.returnValue == false);
    }
}
