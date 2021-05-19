/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package admin.monitoring;

import static admin.monitoring.Constants.*;

/**
 * Enable Monitoring
 * @author Byron Nevins
 */
public class Enabler extends MonTest {
    @Override
    void runTests(TestDriver driver) {
        setDriver(driver);
        report(true, "Enabler says HELLLLLLOOOOO!!!");
        verifyDefaultMainFlags();
        testEnableCommand();
        testSetLevels();
        turnUpMonitoringFullBlast();
    }

    private void turnUpMonitoringFullBlast() {
        for (String config : CONFIG_NAMES) {
            String dot = createDottedAttribute(config, "dtrace");
            report(doesGetMatch(dot, "false"), "-verify-disabled-");
            report(asadmin("enable-monitoring", "--target", config, "--dtrace=true"), "enable-dtrace");
            report(doesGetMatch(dot, "true"), "-verify-enabled-");
            enableMonitoringUsingSet(config, HIGH);
        }
    }

    private void testSetLevels() {
        for (String config : CONFIG_NAMES)
            for (String level : LEVELS)
                enableMonitoringUsingSet(config, level);
    }

    private void verifyDefaultMainFlags() {
        verifyDefaultMainFlags("server");
        verifyDefaultMainFlags(STAND_ALONE_INSTANCE_NAME);
        verifyDefaultMainFlags(CLUSTER_NAME);
    }

    private void verifyDefaultMainFlags(String serverOrClusterName) {
        String reportName = "verify-main-flags " + serverOrClusterName;

        report(doesGetMatch(createDottedAttribute(serverOrClusterName, "dtrace"), "false"),
                reportName + "-dtrace-");
        report(doesGetMatch(createDottedAttribute(serverOrClusterName, "monitoring"), "true"),
                reportName + "-monitoring-");
        report(doesGetMatch(createDottedAttribute(serverOrClusterName, "mbean"), "true"),
                reportName + "-mbean-");
    }

    private void testEnableCommand() {
        testEnableCommand("server");
        testEnableCommand(CLUSTER_NAME);
        testEnableCommand(STAND_ALONE_INSTANCE_NAME);
    }

    private void testEnableCommand(String serverOrClusterName) {
        testDtraceEnableCommand(serverOrClusterName);
        testMbeanEnableCommand(serverOrClusterName);
        testMonitoringEnableCommand(serverOrClusterName);
    }

    private void testDtraceEnableCommand(String serverOrClusterName) {
        // verify off, enable it, verify on, disable, verify off
        String reportName = "dtrace-enable-test ";
        String dot = createDottedAttribute(serverOrClusterName, "dtrace");
        report(doesGetMatch(dot, "false"), reportName + "-verify-disabled-");
        report(asadmin("enable-monitoring", "--target", serverOrClusterName, "--dtrace=true"), reportName + "enable-dtrace");
        report(doesGetMatch(dot, "true"), reportName + "-verify-enabled-");
        report(asadmin("enable-monitoring", "--target", serverOrClusterName, "--dtrace=false"), reportName + "disable-dtrace");
        report(doesGetMatch(dot, "false"), reportName + "-verify-disabled-");
    }

    private void testMbeanEnableCommand(String serverOrClusterName) {
        // verify on, disable it, verify off, enable, verify on
        String reportName = "mbean-enable-test ";
        String dot = createDottedAttribute(serverOrClusterName, "mbean");
        report(doesGetMatch(dot, "true"), reportName + "-verify-enabled-");
        report(asadmin("enable-monitoring", "--target", serverOrClusterName, "--mbean=false"), reportName + "disable-mbean");
        report(doesGetMatch(dot, "false"), reportName + "-verify-disabled-");
        report(asadmin("enable-monitoring", "--target", serverOrClusterName, "--mbean=true"), reportName + "enable-mbean");
        report(doesGetMatch(dot, "true"), reportName + "-verify-enabled-");
    }

    private void testMonitoringEnableCommand(String serverOrClusterName) {
        // verify on, disable it, verify off, enable, verify on
        String reportName = "monitoring-enable-test ";
        String dot = createDottedAttribute(serverOrClusterName, "monitoring");
        report(doesGetMatch(dot, "true"), reportName + "-verify-enabled-");
        report(asadmin("disable-monitoring", "--target", serverOrClusterName), reportName + "disable-all");
        report(doesGetMatch(dot, "false"), reportName + "-verify-disabled-");
        report(asadmin("enable-monitoring", "--target", serverOrClusterName), reportName + "enable-all");
        report(doesGetMatch(dot, "true"), reportName + "-verify-enabled-");
    }

    private void enableMonitoringUsingSet(String serverOrClusterName, String value) {
        final String metName = "enableUsingSet";
        String desiredValue = "=" + value;
        String metFullName = metName + "-set-" + desiredValue + "-";

        for (String monItem : MON_CATEGORIES) {
            String fullitemname = createDottedLevel(serverOrClusterName, monItem);
            String reportname = metFullName + monItem;
            report(asadmin("set", (fullitemname + desiredValue)), reportname);
            report(doesGetMatch(fullitemname, value), reportname + "-verified");
        }
    }
}
