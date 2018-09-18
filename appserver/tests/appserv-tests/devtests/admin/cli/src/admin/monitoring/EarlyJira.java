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

import com.sun.appserv.test.BaseDevTest.AsadminReturn;
import java.io.*;
import static admin.monitoring.Constants.*;

/**
 * Test fixed issues from JIRA
 * These tests depend on monitoring being disabled
 * @author Byron Nevins
 */
public class EarlyJira extends MonTest {
    @Override
    void runTests(TestDriver driver) {
        setDriver(driver);
        report(true, "Hello from Early JIRA Tests!");
        verifyMonOff();
        test15203();
    }

    /*
     * @author Byron Nevins
     * Sanity test -- are the levles all OFF ??
     */
    private void verifyMonOff() {
        String prepend = "verifyMonOff::";
        final String value = OFF;

        for(String cat : MON_CATEGORIES) {
            report(doesGetMatch(das + cat, value), prepend + "das::" + cat);
            report(doesGetMatch(cluster + cat, value), prepend + "mon-cluster::" + cat);
            report(doesGetMatch(standalone + cat, value), prepend + "standalone::" + cat);
        }
    }

    /*
     * @author Byron Nevins
     */
    private void test15203() {
        String prepend = "15203::";

        AsadminReturn aar = asadminWithOutput("get", "-m", STAR);
        report(checkForString(aar, "No monitoring data to report.", 3), prepend + "verify special message all 3 --");
        report(!checkForString(aar, "No monitoring data to report.", 4), prepend + "verify 3 only");

        for (String iname : INSTANCES) {
            aar = asadminWithOutput("get", "-m", makestar(iname));
            report(checkForString(aar, "No monitoring data to report."), prepend + "verify special message " + iname);
        }
    }

    // what a pain!
    private String makestar(String iname) {
        if (isWindows)
            return "\"" + iname + ".*\"";
        else
            return iname + ".*";
    }
    String das = "configs.config.server-config.monitoring-service.module-monitoring-levels.";
    String cluster = "configs.config.mon-cluster-config.monitoring-service.module-monitoring-levels.";
    String standalone = "configs.config.standalone-i3-config.monitoring-service.module-monitoring-levels.";
}
