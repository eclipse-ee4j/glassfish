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

package admin;

/**
 *
 * This will test miscellaneous commands that don't have a dedicated test file
 * @author Tom Mueller
 */
public class MiscCommandsTest extends AdminBaseDevTest {

    @Override
    protected String getTestDescription() {
        return "Miscellaneous Commands Test";
    }

    public static void main(String[] args) {
        new MiscCommandsTest().runTests();    }

    private void runTests() {
        testVersion();
        testMulticastValidator();
        stat.printSummary();
    }

    private void testVersion() {

        final String tn = "version-";
        report(tn + "dasdown-norm", asadmin("version"));
        report(tn + "dasdown-local", asadmin("version", "--local"));
        report(tn + "JIRA15552-das-stopped", asadmin("version", "--local", "--terse", "--verbose"));
        startDomain();
        report(tn + "dasup-norm", asadmin("version"));
        report(tn + "dasup-local", asadmin("version", "--local"));
        report(tn + "JIRA15552-das-running", asadmin("version", "--local", "--terse", "--verbose"));
        stopDomain();
    }

    private void testMulticastValidator() {
        final int defaultSeconds = 20;

        long time0 = System.currentTimeMillis();

        // should not fail if multicast is not available
        asadmin("validate-multicast");
        long time1 = System.currentTimeMillis();

        // should take at least 20 seconds
        boolean success = (time1-time0) > (1000 * defaultSeconds);
        report("validate-multicast-timing", success);

        // now with params
        final String port = "2049";
        final String address = "228.9.3.3";
        final String period = "900";
        final int seconds = 5;
        time0 = System.currentTimeMillis();
        AsadminReturn ret = asadminWithOutput("validate-multicast",
            "--multicastport", port,
            "--multicastaddress", address,
            "--sendperiod", period,
            "--timeout", String.valueOf(seconds));
        time1 = System.currentTimeMillis();
        String out = ret.outAndErr;
        report("validate-multicast-param-port",
            out.contains(port));
        report("validate-multicast-param-address",
            out.contains(address));
        report("validate-multicast-param-period",
            out.contains(period));
        report("validate-multicast-param-seconds",
            out.contains(String.valueOf(seconds)));

        // should only take a little over 5 seconds
        int atLeast = seconds - 1;
        int notThisLong = seconds + 8; // wide berth here
        report("validate-multicast-param-timing-under",
            (time1-time0) > 1000*atLeast);
        report("validate-multicast-param-timing-over",
            (time1-time0) < 1000*notThisLong);
    }
}
