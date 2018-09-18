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
 * Setup the outside world for Monitoring Dev Tests
 * @author Byron Nevins
 */
public class Setup extends MonTest {
    @Override
    void runTests(TestDriver driver) {
        setDriver(driver);
        report(true, "Setup here!!!");
        createDomain();
        startDomain();
        setupJvmMemory();
        report(asadmin("stop-domain", DOMAIN_NAME), "stop-with-new-jvm-memory");
        startDomain();
        test13723();
        createCluster();
        createInstances();
        startInstances();
    }

    /**
     * The test is here - not in Jira.java because we can NOT have any instances
     * running for this test to work.
     * list -m "*" did not work
     * list -m "server.*" was needed
     */
    private void test13723() {
        final String prepend = "test13723::";

        // before the fix server.jvm would NOT be in there
        report(!checkForString(asadminWithOutput("list", "-m", STAR), "server.jvm"),
                prepend + "just star");
        report(!checkForString(asadminWithOutput("list", "-m", SERVERDOTSTAR), "server.jvm"),
                prepend + "server dot star");

        report(asadmin("enable-monitoring", "--modules", "jvm=HIGH"), prepend + "enable-mon");
        report(checkForString(asadminWithOutput("list", "-m", STAR), "server.jvm"),
                prepend + "just star");
        report(checkForString(asadminWithOutput("list", "-m", SERVERDOTSTAR), "server.jvm"),
                prepend + "server dot star");

        // return to original state!
        report(asadmin("enable-monitoring", "--modules", "jvm=OFF"), prepend + "disable-mon");
        report(!checkForString(asadminWithOutput("list", "-m", STAR), "server.jvm"),
                prepend + "just star");
        report(!checkForString(asadminWithOutput("list", "-m", SERVERDOTSTAR), "server.jvm"),
                prepend + "server dot star");
    }
}
